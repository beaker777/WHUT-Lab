import type { DifficultyKey, GameBoardResponse, GameRuleConfig, HintResponse, MatchCheckResponse } from "../types/game";

export interface MatchCheckPayload {
  map: number[][];
  config: GameRuleConfig;
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

export async function initGameBoard(difficulty: DifficultyKey): Promise<GameBoardResponse> {
  const response = await fetch("/api/game/init", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ difficulty })
  });

  return parseJsonResponse<GameBoardResponse>(response);
}

export async function shuffleGameBoard(map: number[][], config: GameRuleConfig): Promise<GameBoardResponse> {
  const response = await fetch("/api/game/shuffle", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ map, config })
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

export async function fetchHint(map: number[][], config: GameRuleConfig): Promise<HintResponse> {
  const response = await fetch("/api/game/hint", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ map, config })
  });

  return parseJsonResponse<HintResponse>(response);
}
