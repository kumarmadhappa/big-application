import cookieParser from 'cookie-parser';
import express, { type NextFunction, type Request, type Response } from 'express';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const app = express();
const port = Number(process.env.PORT ?? 4000);
const apiGatewayUrl = process.env.API_GATEWAY_URL ?? 'http://localhost:8080';
const accessCookieName = process.env.ACCESS_COOKIE_NAME ?? 'ui_access_token';
const refreshCookieName = process.env.REFRESH_COOKIE_NAME ?? 'ui_refresh_token';
const isProduction = process.env.NODE_ENV === 'production';
const distPath = path.resolve(__dirname, '..', 'dist');

app.use(cookieParser());
app.use(express.json());

app.get('/api/session', async (req, res) => {
  const accessToken = req.cookies[accessCookieName];
  const user = accessToken ? decodeJwtUser(accessToken) : undefined;
  res.json({ authenticated: Boolean(accessToken), user });
});

app.post('/api/auth/register', async (req, res) => {
  await handleAuthForward(req, res, '/api/auth/register');
});

app.post('/api/auth/login', async (req, res) => {
  await handleAuthForward(req, res, '/api/auth/login');
});

app.post('/api/banking/auth/login', async (req, res) => {
  await handleAuthForward(req, res, '/api/banking/auth/login');
});

app.post('/api/auth/refresh', async (req, res) => {
  const refreshToken = req.cookies[refreshCookieName];
  if (!refreshToken) {
    res.status(401).json({ success: false, message: 'Missing refresh token' });
    return;
  }

  await handleAuthForward(req, res, '/api/auth/refresh', { refreshToken });
});

app.post('/api/banking/auth/refresh', async (req, res) => {
  const refreshToken = req.cookies[refreshCookieName];
  if (!refreshToken) {
    res.status(401).json({ success: false, message: 'Missing refresh token' });
    return;
  }

  await handleAuthForward(req, res, '/api/banking/auth/refresh', { refreshToken });
});

app.post('/api/auth/logout', (req, res) => {
  clearAuthCookies(res);
  res.json({ success: true });
});

app.use('/api/users', async (req, res) => {
  await handleProxyRequest(req, res);
});

app.use('/api/banking', async (req, res) => {
  await handleProxyRequest(req, res);
});

app.get('/healthz', (_req, res) => {
  res.json({ ok: true });
});

if (fs.existsSync(distPath)) {
  app.use(express.static(distPath));
  app.get('*', (_req, res) => {
    res.sendFile(path.join(distPath, 'index.html'));
  });
}

app.use((err: unknown, _req: Request, res: Response, _next: NextFunction) => {
  console.error(err);
  res.status(500).json({ success: false, message: 'Internal server error' });
});

app.listen(port, () => {
  console.log(`UI server listening on http://localhost:${port}`);
});

async function handleAuthForward(
  req: Request,
  res: Response,
  upstreamPath: string,
  extraBody?: Record<string, unknown>
) {
  console.info(`Forwarding ${req.method} ${req.originalUrl} -> ${upstreamPath}`);
  const upstream = await fetch(`${apiGatewayUrl}${upstreamPath}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(extraBody ?? req.body ?? {})
  });

  const payload = await readUpstreamBody(upstream);
  console.info(`Upstream ${upstreamPath} returned ${upstream.status}`);
  if (upstream.ok && typeof payload === 'object' && payload !== null && 'data' in payload) {
    const data = (payload as { data?: { accessToken?: string; refreshToken?: string } }).data;
    if (data?.accessToken && data?.refreshToken) {
      setAuthCookies(res, data.accessToken, data.refreshToken);
    }
  }

  if (typeof payload === 'string') {
    res.status(upstream.status).type(upstream.headers.get('content-type') ?? 'text/plain').send(payload);
    return;
  }

  res.status(upstream.status).json(payload);
}

async function handleProxyRequest(req: express.Request, res: express.Response) {
  const accessToken = req.cookies[accessCookieName];
  if (!accessToken) {
    res.status(401).json({ success: false, message: 'Missing access token' });
    return;
  }

  const targetUrl = `${apiGatewayUrl}${req.originalUrl}`;
  console.info(`Proxying ${req.method} ${req.originalUrl} -> ${targetUrl}`);
  const apiResponse = await fetch(targetUrl, {
    method: req.method,
    headers: buildForwardHeaders(req.headers, accessToken),
    body: hasBody(req.method) ? JSON.stringify(req.body ?? {}) : undefined
  });

  await writeUpstreamResponse(res, apiResponse);
}

function buildForwardHeaders(headers: Request['headers'], accessToken: string) {
  const forwarded = new Headers();
  forwarded.set('Authorization', `Bearer ${accessToken}`);
  forwarded.set('Accept', 'application/json');
  if (headers['content-type']) {
    forwarded.set('Content-Type', String(headers['content-type']));
  }
  return forwarded;
}

function hasBody(method: string) {
  return !['GET', 'HEAD'].includes(method.toUpperCase());
}

async function writeUpstreamResponse(res: Response, upstream: globalThis.Response) {
  const text = await upstream.text();
  const contentType = upstream.headers.get('content-type') ?? 'application/json';
  res.status(upstream.status);
  res.type(contentType);
  res.send(text);
}

async function readUpstreamBody(upstream: globalThis.Response) {
  const text = await upstream.text();
  if (!text) {
    return null;
  }

  const contentType = upstream.headers.get('content-type') ?? '';
  if (contentType.includes('application/json')) {
    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  }

  return text;
}

function setAuthCookies(res: Response, accessToken: string, refreshToken: string) {
  const cookieOptions = {
    httpOnly: true,
    sameSite: 'lax' as const,
    secure: isProduction,
    path: '/'
  };

  res.cookie(accessCookieName, accessToken, cookieOptions);
  res.cookie(refreshCookieName, refreshToken, cookieOptions);
}

function clearAuthCookies(res: Response) {
  res.clearCookie(accessCookieName, { path: '/' });
  res.clearCookie(refreshCookieName, { path: '/' });
}

function decodeJwtUser(token: string) {
  const parts = token.split('.');
  if (parts.length < 2) {
    return undefined;
  }

  try {
    const payload = JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8')) as {
      sub?: string;
      roles?: string[];
      email?: string;
    };

    if (!payload.sub) {
      return undefined;
    }

    return {
      username: payload.sub,
      email: payload.email ?? '',
      roles: Array.isArray(payload.roles) ? payload.roles : []
    };
  } catch {
    return undefined;
  }
}
