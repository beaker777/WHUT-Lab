import type { GameBoardResponse, HintResponse, MatchCheckResponse } from "../types/game";

export interface MatchCheckPayload {
  map: number[][];
  v1: {
    row: number;
    col: number;
    type: number;
  };
  v2: {
    row: number;
    col: number;
    type: number;
  };
}

async function parseJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error("接口调用失败");
  }

  return response.json() as Promise<T>;
}

export async function initGameBoard(): Promise<GameBoardResponse> {
  const response = await fetch("/api/game/init");
  return parseJsonResponse<GameBoardResponse>(response);
}

export async function shuffleGameBoard(map: number[][]): Promise<GameBoardResponse> {
  const response = await fetch("/api/game/shuffle", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ map })
  });

  return parseJsonResponse<GameBoardResponse>(response);
}

export async function matchCheck(payload: MatchCheckPayload): Promise<MatchCheckResponse> {
  const response = await fetch("/api/game/match-check", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });

  return parseJsonResponse<MatchCheckResponse>(response);
}

export async function fetchHint(map: number[][]): Promise<HintResponse> {
  const response = await fetch("/api/game/hint", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ map })
  });

  return parseJsonResponse<HintResponse>(response);
}
