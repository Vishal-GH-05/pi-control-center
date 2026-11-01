import type {
    CpuStats,
    FileSystemStats,
    LoadStats,
    MemoryStats,
    NetworkStats,
    SensorStats,
} from '../types/current';

import type {
    CpuHistory,
    LoadHistory,
    MemoryHistory,
    NetworkHistory,
    SensorHistory,
} from '../types/history';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

async function get<T>(endpoint: string): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${endpoint}`);

    if (!response.ok) {
        throw new Error(
            `API request failed: ${response.status} ${response.statusText}`,
        );
    }

    const contentType = response.headers.get('content-type');

    if (contentType?.includes('application/json')) {
        return (await response.json()) as T;
    }

    return (await response.text()) as T;
}

export const currentApi = {
    cpu: () => get<CpuStats>('/app/current/system/cpu'),
    memory: () => get<MemoryStats>('/app/current/system/memory'),
    load: () => get<LoadStats>('/app/current/system/load'),
    network: () => get<NetworkStats>('/app/current/system/network'),
    filesystem: () => get<FileSystemStats>('/app/current/system/fs'),
    sensor: () => get<SensorStats>('/app/current/system/sensor'),
    uptime: () => get<string>('/app/current/system/uptime'),
};

export const historyApi = {
    cpu: () => get<CpuHistory[]>('/app/historical/system/cpu'),
    memory: () => get<MemoryHistory[]>('/app/historical/system/memory'),
    load: () => get<LoadHistory[]>('/app/historical/system/load'),
    network: () => get<NetworkHistory[]>('/app/historical/system/network'),
    sensor: () => get<SensorHistory[]>('/app/historical/system/sensor'),
};