import { useEffect, useRef } from 'react';
import { InstructionResponse } from '../types';

type UseLiveStatusParams = {
  enabled: boolean;
  onMessage: (instruction: InstructionResponse) => void;
  poller: () => Promise<void>;
};

const WS_RECONNECT_INTERVAL = 5000;

const resolveWebSocketUrl = () => {
  const socketUrl = import.meta.env.VITE_WS_BASE_URL;
  if (socketUrl) {
    return socketUrl;
  }

  const { protocol, host } = window.location;
  const wsProtocol = protocol === 'https:' ? 'wss:' : 'ws:';
  return `${wsProtocol}//${host}/ws/updates`;
};

const useLiveStatus = ({ enabled, onMessage, poller }: UseLiveStatusParams) => {
  const wsRef = useRef<WebSocket | null>(null);
  const pollInterval = useRef<number | null>(null);

  useEffect(() => {
    if (!enabled) {
      return () => undefined;
    }

    const setupPolling = () => {
      poller().catch((error) => console.error('Polling error', error));
      pollInterval.current = window.setInterval(() => {
        poller().catch((error) => console.error('Polling error', error));
      }, 10000);
    };

    const connectWebSocket = () => {
      try {
        const ws = new WebSocket(resolveWebSocketUrl());
        wsRef.current = ws;

        ws.onmessage = (event) => {
          try {
            const payload = JSON.parse(event.data) as InstructionResponse;
            onMessage(payload);
          } catch (error) {
            console.error('Failed to parse websocket payload', error);
          }
        };

        ws.onopen = () => {
          if (pollInterval.current) {
            clearInterval(pollInterval.current);
            pollInterval.current = null;
          }
        };

        ws.onclose = () => {
          wsRef.current = null;
          if (!pollInterval.current) {
            setupPolling();
          }
          window.setTimeout(connectWebSocket, WS_RECONNECT_INTERVAL);
        };

        ws.onerror = (error) => {
          console.error('WebSocket error', error);
          ws.close();
        };
      } catch (error) {
        console.error('WebSocket setup failed, falling back to polling', error);
        setupPolling();
      }
    };

    connectWebSocket();
    setupPolling();

    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
      if (pollInterval.current) {
        clearInterval(pollInterval.current);
      }
    };
  }, [enabled, onMessage, poller]);
};

export default useLiveStatus;
