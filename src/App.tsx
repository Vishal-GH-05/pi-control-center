import { useCallback, useEffect, useMemo, useState } from 'react';
import './App.css';

import { currentApi, historyApi } from './api/metricsApi';

import type {
  CpuStats,
  FileSystemStats,
  LoadStats,
  MemoryStats,
  NetworkStats,
  SensorStats,
} from './types/current';

import type {
  CpuHistory,
  LoadHistory,
  MemoryHistory,
  NetworkHistory,
  SensorHistory,
} from './types/history';

type CurrentMetrics = {
  cpu: CpuStats | null;
  memory: MemoryStats | null;
  load: LoadStats | null;
  network: NetworkStats | null;
  filesystem: FileSystemStats | null;
  sensor: SensorStats | null;
  uptime: string | null;
};

type HistoricalMetrics = {
  cpu: CpuHistory[];
  memory: MemoryHistory[];
  load: LoadHistory[];
  network: NetworkHistory[];
  sensor: SensorHistory[];
};

const emptyCurrent: CurrentMetrics = {
  cpu: null,
  memory: null,
  load: null,
  network: null,
  filesystem: null,
  sensor: null,
  uptime: null,
};

const emptyHistory: HistoricalMetrics = {
  cpu: [],
  memory: [],
  load: [],
  network: [],
  sensor: [],
};

function formatBytes(bytes?: number | null): string {
  if (bytes == null || !Number.isFinite(bytes)) return '—';

  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let value = bytes;
  let unit = 0;

  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024;
    unit += 1;
  }

  return `${value.toFixed(unit === 0 ? 0 : 1)} ${units[unit]}`;
}

function formatRate(value?: number | null): string {
  return value == null || !Number.isFinite(value)
      ? '—'
      : `${formatBytes(value)}/s`;
}

function shortTime(value?: string): string {
  if (!value) return '';

  const date = new Date(value);

  return Number.isNaN(date.getTime())
      ? value
      : date.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
      });
}

function Sparkline({ values }: { values: number[] }) {
  if (values.length < 2) {
    return <div className="sparkline-empty" />;
  }

  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = Math.max(max - min, 1);

  const points = values
      .map((value, index) => {
        const x = (index / (values.length - 1)) * 280;
        const y = 34 - ((value - min) / range) * 27;

        return `${x},${y}`;
      })
      .join(' ');

  return (
      <svg
          className="sparkline"
          viewBox="0 0 280 38"
          preserveAspectRatio="none"
          aria-hidden="true"
      >
        <polyline
            points={points}
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
        />
      </svg>
  );
}

function MetricCard({
                      title,
                      value,
                      unit,
                      detail,
                      tone,
                      icon,
                      trend,
                    }: {
  title: string;
  value: string;
  unit?: string;
  detail: string;
  tone: 'purple' | 'green' | 'orange' | 'cyan';
  icon: string;
  trend: number[];
}) {
  return (
      <article className={`metric-card ${tone}`}>
        <div className="metric-topline">
          <h2>{title}</h2>
          <span className="metric-symbol" aria-hidden="true">
          {icon}
        </span>
        </div>

        <div className="metric-number">
          {value}
          <span>{unit}</span>
        </div>

        <p>{detail}</p>

        <Sparkline values={trend} />
      </article>
  );
}

function HistoryChart({
                        title,
                        description,
                        rows,
                        series,
                        maxValue,
                      }: {
  title: string;
  description?: string;
  rows: Array<{
    recordedAt: string;
    [key: string]: string | number;
  }>;
  series: Array<{
    key: string;
    label: string;
    color: string;
  }>;
  maxValue?: number;
}) {
  const width = 700;
  const height = 220;
  const left = 38;
  const right = 8;
  const top = 12;
  const bottom = 24;

  const plotWidth = width - left - right;
  const plotHeight = height - top - bottom;

  const values = rows.flatMap((row) =>
      series
          .map((item) => Number(row[item.key]))
          .filter(Number.isFinite),
  );

  const ceiling = maxValue ?? Math.max(1, ...values) * 1.15;

  const makePath = (key: string) =>
      rows
          .map((row, index) => {
            const x =
                left +
                (rows.length < 2
                    ? plotWidth / 2
                    : (index / (rows.length - 1)) * plotWidth);

            const value = Math.max(0, Number(row[key]) || 0);
            const y =
                top + plotHeight - (value / ceiling) * plotHeight;

            return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
          })
          .join(' ');

  return (
      <article className="panel chart-panel">
        <div className="panel-heading">
          <div>
            <h2>{title}</h2>
            {description && <p>{description}</p>}
          </div>

          <span className="chart-tag">HISTORY</span>
        </div>

        {rows.length === 0 ? (
            <div className="chart-empty">
              Historical records will appear here when available.
            </div>
        ) : (
            <>
              <div className="chart-canvas">
                <svg
                    viewBox={`0 0 ${width} ${height}`}
                    role="img"
                    aria-label={`${title} history chart`}
                >
                  {[0, 1, 2, 3].map((tick) => {
                    const y = top + (tick / 3) * plotHeight;

                    return (
                        <line
                            key={tick}
                            x1={left}
                            x2={width - right}
                            y1={y}
                            y2={y}
                            className="grid-line"
                        />
                    );
                  })}

                  {series.map((item) => (
                      <path
                          key={item.key}
                          d={makePath(item.key)}
                          fill="none"
                          stroke={item.color}
                          strokeWidth="3"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                      />
                  ))}
                </svg>
              </div>

              <div className="chart-times">
                <span>{shortTime(rows[0]?.recordedAt)}</span>
                <span>
              {shortTime(
                  rows[Math.floor(rows.length / 2)]?.recordedAt,
              )}
            </span>
                <span>
              {shortTime(rows[rows.length - 1]?.recordedAt)}
            </span>
              </div>
            </>
        )}

        <div className="chart-legend">
          {series.map((item) => (
              <span key={item.key}>
            <i style={{ background: item.color }} />
                {item.label}
          </span>
          ))}
        </div>
      </article>
  );
}

function MemoryPanel({
                       memory,
                     }: {
  memory: MemoryStats | null;
}) {
  const percent = Math.max(
      0,
      Math.min(100, memory?.percent ?? 0),
  );

  return (
      <article className="panel memory-panel">
        <div className="panel-heading">
          <div>
            <h2>Memory Usage</h2>
            <p>RAM utilization</p>
          </div>
        </div>

        <div className="memory-layout">
          <div
              className="memory-donut"
              style={{
                background: `conic-gradient(#9865ff ${percent}%, #34343e 0)`,
              }}
          >
            <div>
              <strong>
                {memory ? `${percent.toFixed(1)}%` : '—'}
              </strong>
              <span>utilized</span>
            </div>
          </div>

          <div className="memory-keys">
            <div>
              <i className="used-dot" />
              <span>
              Used
              <strong>
                {memory ? formatBytes(memory.used) : '—'}
              </strong>
            </span>
            </div>

            <div>
              <i className="free-dot" />
              <span>
              Free
              <strong>
                {memory ? formatBytes(memory.free) : '—'}
              </strong>
            </span>
            </div>

            <div>
              <i className="total-dot" />
              <span>
              Total
              <strong>
                {memory ? formatBytes(memory.total) : '—'}
              </strong>
            </span>
            </div>
          </div>
        </div>
      </article>
  );
}

export default function App() {
  const [current, setCurrent] =
      useState<CurrentMetrics>(emptyCurrent);

  const [history, setHistory] =
      useState<HistoricalMetrics>(emptyHistory);

  const [apiOnline, setApiOnline] = useState(false);
  const [updatedAt, setUpdatedAt] = useState<Date | null>(null);
  const [currentError, setCurrentError] = useState<string | null>(
      null,
  );
  const [historyError, setHistoryError] = useState<string | null>(
      null,
  );

  const refreshCurrent = useCallback(async () => {
    const results = await Promise.allSettled([
      currentApi.cpu(),
      currentApi.memory(),
      currentApi.load(),
      currentApi.network(),
      currentApi.filesystem(),
      currentApi.sensor(),
      currentApi.uptime(),
    ]);

    const [
      cpu,
      memory,
      load,
      network,
      filesystem,
      sensor,
      uptime,
    ] = results;

    const failures = results.filter(
        (item) => item.status === 'rejected',
    ).length;

    setCurrent((previous) => ({
      cpu: cpu.status === 'fulfilled' ? cpu.value : previous.cpu,
      memory:
          memory.status === 'fulfilled'
              ? memory.value
              : previous.memory,
      load:
          load.status === 'fulfilled' ? load.value : previous.load,
      network:
          network.status === 'fulfilled'
              ? network.value
              : previous.network,
      filesystem:
          filesystem.status === 'fulfilled'
              ? filesystem.value
              : previous.filesystem,
      sensor:
          sensor.status === 'fulfilled'
              ? sensor.value
              : previous.sensor,
      uptime:
          uptime.status === 'fulfilled'
              ? uptime.value
              : previous.uptime,
    }));

    setApiOnline(
        results.some((item) => item.status === 'fulfilled'),
    );

    setCurrentError(
        failures === results.length
            ? 'Unable to reach the backend API. Check the connection.'
            : failures > 0
                ? `${failures} current metric request(s) failed.`
                : null,
    );

    setUpdatedAt(new Date());
  }, []);

  const refreshHistory = useCallback(async () => {
    const results = await Promise.allSettled([
      historyApi.cpu(),
      historyApi.memory(),
      historyApi.load(),
      historyApi.network(),
      historyApi.sensor(),
    ]);

    const [cpu, memory, load, network, sensor] = results;

    const failures = results.filter(
        (item) => item.status === 'rejected',
    ).length;

    setHistory((previous) => ({
      cpu: cpu.status === 'fulfilled' ? cpu.value : previous.cpu,
      memory:
          memory.status === 'fulfilled'
              ? memory.value
              : previous.memory,
      load:
          load.status === 'fulfilled' ? load.value : previous.load,
      network:
          network.status === 'fulfilled'
              ? network.value
              : previous.network,
      sensor:
          sensor.status === 'fulfilled'
              ? sensor.value
              : previous.sensor,
    }));

    setHistoryError(
        failures ? `${failures} historical request(s) failed.` : null,
    );
  }, []);

  useEffect(() => {
    void refreshCurrent();
    void refreshHistory();

    const timer = window.setInterval(
        () => void refreshCurrent(),
        5000,
    );

    return () => window.clearInterval(timer);
  }, [refreshCurrent, refreshHistory]);

  const cpuTrend = useMemo(
      () => history.cpu.slice(-35).map((row) => row.total),
      [history.cpu],
  );

  const memoryTrend = useMemo(
      () => history.memory.slice(-35).map((row) => row.percent),
      [history.memory],
  );

  const loadTrend = useMemo(
      () => history.load.slice(-35).map((row) => row.min1),
      [history.load],
  );

  const networkTrend = useMemo(
      () =>
          history.network
              .slice(-35)
              .map((row) => row.recvRatePerSec),
      [history.network],
  );

  const cpuRows = history.cpu.slice(-60).map((row) => ({
    recordedAt: row.recordedAt,
    total: row.total,
  }));

  const loadRows = history.load.slice(-60).map((row) => ({
    recordedAt: row.recordedAt,
    min1: row.min1,
    min5: row.min5,
    min15: row.min15,
  }));

  const memoryRows = history.memory.slice(-60).map(
      (row: MemoryHistory) => ({
        recordedAt: row.recordedAt,
        percent: row.percent,
      }),
  );

  const networkRows = history.network.slice(-60).map(
      (row: NetworkHistory) => ({
        recordedAt: row.recordedAt,
        receive: row.recvRatePerSec,
      }),
  );

  const sensorRows = history.sensor.slice(-60).map(
      (row: SensorHistory) => ({
        recordedAt: row.recordedAt,
        temperature: row.value,
      }),
  );

  const networkValue = current.network
      ? `${formatRate(current.network.recvRatePerSec)} / ${formatRate(
          current.network.sentRatePerSec,
      )}`
      : '—';

  return (
      <div className="app-shell">
        <aside className="sidebar">
          <div className="brand-slot" aria-label="Logo placeholder" />

          <nav
              className="sidebar-nav"
              aria-label="Dashboard navigation"
          >
            <a className="nav-item selected" href="#overview">
              <span>⌂</span>
              <b>Overview</b>
            </a>

            <a className="nav-item" href="#history">
              <span>▤</span>
              <b>History</b>
            </a>

            <a className="nav-item" href="#system">
              <span>⚙</span>
              <b>System</b>
            </a>

            <a className="nav-item" href="#about">
              <span>ⓘ</span>
              <b>About</b>
            </a>
          </nav>

          <div className="sidebar-device">
            <div
                className="device-image-slot"
                aria-label="Device image placeholder"
            />

            <div className="device-label">
              <strong>Raspberry Pi</strong>
              <span>Control node</span>
            </div>

            <span className="more-dots">···</span>
          </div>
        </aside>

        <main className="main-content" id="overview">
          <header className="topbar">
            <div>
              <p className="eyebrow">SYSTEM MONITOR</p>
              <h1>
                Pi Control <span>Center</span>
              </h1>
            </div>

            <div className="topbar-status">
            <span
                className={`status-dot ${
                    apiOnline ? 'is-online' : ''
                }`}
            />

              <span>
              {apiOnline ? 'System online' : 'Connecting'}
            </span>

              <span className="status-divider" />
              <span className="poll-label">Refresh · 5s</span>
            </div>
          </header>

          <div className="content-wrap">
            {currentError && (
                <div className="error-banner">{currentError}</div>
            )}

            <section
                className="metric-grid"
                aria-label="Live system metrics"
            >
              <MetricCard
                  title="CPU Usage"
                  value={
                    current.cpu ? current.cpu.total.toFixed(1) : '—'
                  }
                  unit="%"
                  detail={
                    current.cpu
                        ? `${current.cpu.cores} cores · User ${current.cpu.user.toFixed(
                            1,
                        )}%`
                        : 'Waiting for metrics'
                  }
                  tone="purple"
                  icon="▦"
                  trend={cpuTrend}
              />

              <MetricCard
                  title="Memory Usage"
                  value={
                    current.memory
                        ? current.memory.percent.toFixed(1)
                        : '—'
                  }
                  unit="%"
                  detail={
                    current.memory
                        ? `${formatBytes(current.memory.used)} in use`
                        : 'Waiting for metrics'
                  }
                  tone="green"
                  icon="▤"
                  trend={memoryTrend}
              />

              <MetricCard
                  title="Load Average"
                  value={
                    current.load ? current.load.min1.toFixed(2) : '—'
                  }
                  detail={
                    current.load
                        ? `5m ${current.load.min5.toFixed(
                            2,
                        )} · 15m ${current.load.min15.toFixed(2)}`
                        : 'Waiting for metrics'
                  }
                  tone="orange"
                  icon="⌁"
                  trend={loadTrend}
              />

              <MetricCard
                  title="Network Traffic"
                  value={networkValue}
                  detail={current.network?.name ?? 'Waiting for metrics'}
                  tone="cyan"
                  icon="⌁"
                  trend={networkTrend}
              />
            </section>

            <section className="main-grid">
              <HistoryChart
                  title="CPU Performance"
                  description="Processor utilization over time"
                  rows={cpuRows}
                  series={[
                    {
                      key: 'total',
                      label: 'CPU usage',
                      color: '#9865ff',
                    },
                  ]}
                  maxValue={100}
              />

              <article className="panel device-panel">
                <div className="device-panel-head">
                  <div>
                    <p className="eyebrow">DEVICE STATUS</p>
                    <h2>Raspberry Pi</h2>
                  </div>

                  <span className="online-pill">
                  <i />
                  Online
                </span>
                </div>

                <div className="device-row">
                  <span>Uptime</span>
                  <strong>{current.uptime ?? '—'}</strong>
                </div>

                <div className="device-row">
                  <span>CPU temperature</span>
                  <strong>
                    {current.sensor
                        ? `${current.sensor.value} ${current.sensor.unit}`
                        : '—'}
                  </strong>
                </div>

                <div className="device-row">
                  <span>CPU cores</span>
                  <strong>{current.cpu?.cores ?? '—'}</strong>
                </div>

                <div className="device-row">
                  <span>Filesystem usage</span>
                  <strong>
                    {current.filesystem
                        ? `${current.filesystem.percent.toFixed(1)}%`
                        : '—'}
                  </strong>
                </div>

                <div className="device-updated">
                  {updatedAt
                      ? `Last updated ${updatedAt.toLocaleTimeString()}`
                      : 'Waiting for first update'}
                </div>
              </article>
            </section>

            <section className="lower-grid">
              <MemoryPanel memory={current.memory} />

              <HistoryChart
                  title="System Load"
                  description="1, 5 and 15 minute averages"
                  rows={loadRows}
                  series={[
                    {
                      key: 'min1',
                      label: '1 min',
                      color: '#39bdf5',
                    },
                    {
                      key: 'min5',
                      label: '5 min',
                      color: '#9865ff',
                    },
                    {
                      key: 'min15',
                      label: '15 min',
                      color: '#f5a044',
                    },
                  ]}
              />

              <article className="panel temperature-panel">
                <div className="temperature-title">
                  <span className="temperature-icon">♨</span>
                  <div>
                    <h2>CPU Temperature</h2>
                    <p>Thermal sensor</p>
                  </div>
                </div>

                <strong>
                  {current.sensor
                      ? `${current.sensor.value} ${current.sensor.unit}`
                      : '—'}
                </strong>

                <div className="temperature-meter">
                <span
                    style={{
                      width: `${Math.min(
                          100,
                          Math.max(
                              0,
                              Number(current.sensor?.value ?? 0) * 1.5,
                          ),
                      )}%`,
                    }}
                />
                </div>

                <small>
                  {current.sensor?.label ?? 'Waiting for sensor data'}
                </small>
              </article>
            </section>

            <section className="history-section" id="history">
              <div className="section-heading">
                <div>
                  <p className="eyebrow">POSTGRESQL</p>
                  <h2>Historical Metrics</h2>
                  <p>Previously collected system data</p>
                </div>

                <button
                    type="button"
                    onClick={() => void refreshHistory()}
                >
                  Refresh history ↻
                </button>
              </div>

              {historyError && (
                  <div className="error-banner">{historyError}</div>
              )}

              <div className="history-grid">
                <HistoryChart
                    title="Memory History"
                    rows={memoryRows}
                    series={[
                      {
                        key: 'percent',
                        label: 'Memory usage',
                        color: '#27d69a',
                      },
                    ]}
                    maxValue={100}
                />

                <HistoryChart
                    title="Network Receive"
                    rows={networkRows}
                    series={[
                      {
                        key: 'receive',
                        label: 'Bytes / sec',
                        color: '#39bdf5',
                      },
                    ]}
                />

                <HistoryChart
                    title="Sensor History"
                    rows={sensorRows}
                    series={[
                      {
                        key: 'temperature',
                        label: history.sensor[0]?.unit ?? 'Sensor',
                        color: '#f5a044',
                      },
                    ]}
                />
              </div>
            </section>

            <section className="system-strip" id="system">
              <div>
                <span>Network interface</span>
                <strong>{current.network?.name ?? '—'}</strong>
              </div>

              <div>
                <span>Filesystem device</span>
                <strong>
                  {current.filesystem?.deviceName ?? '—'}
                </strong>
              </div>

              <div>
                <span>Mount point</span>
                <strong>{current.filesystem?.mntPoint ?? '—'}</strong>
              </div>

              <div>
                <span>Storage available</span>
                <strong>
                  {formatBytes(current.filesystem?.free)}
                </strong>
              </div>
            </section>

            <footer className="footer" id="about">
              <span>Pi Control Center</span>
              <span>
              REST API <i /> 5-second polling <i /> No WebSockets
            </span>
            </footer>
          </div>
        </main>
      </div>
  );
}