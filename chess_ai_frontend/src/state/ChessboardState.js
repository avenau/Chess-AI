import { useSyncExternalStore } from 'react'
import { createStore } from 'redux'
import { Chess } from 'chess.js'

const INITIAL_FEN = new Chess().fen()

const initialState = {
  fen: INITIAL_FEN,
  status: 'Choose a game mode to start.',
  isThinking: false,
  lastMove: null,
  requestBody: JSON.stringify({ fen: INITIAL_FEN }, null, 2),
  gameMode: null,
  playerColor: 'white',
  setupOpen: true,
}

const SET_FEN = 'chessboard/SET_FEN'
const SET_STATUS = 'chessboard/SET_STATUS'
const SET_IS_THINKING = 'chessboard/SET_IS_THINKING'
const SET_LAST_MOVE = 'chessboard/SET_LAST_MOVE'
const SET_REQUEST_BODY = 'chessboard/SET_REQUEST_BODY'
const SET_GAME_MODE = 'chessboard/SET_GAME_MODE'
const SET_PLAYER_COLOR = 'chessboard/SET_PLAYER_COLOR'
const SET_SETUP_OPEN = 'chessboard/SET_SETUP_OPEN'
const RESET_CHESSBOARD = 'chessboard/RESET_CHESSBOARD'

function chessboardReducer(state = initialState, action) {
  switch (action.type) {
    case SET_FEN:
      return { ...state, fen: action.payload }
    case SET_STATUS:
      return { ...state, status: action.payload }
    case SET_IS_THINKING:
      return { ...state, isThinking: action.payload }
    case SET_LAST_MOVE:
      return { ...state, lastMove: action.payload }
    case SET_REQUEST_BODY:
      return { ...state, requestBody: action.payload }
    case SET_GAME_MODE:
      return { ...state, gameMode: action.payload }
    case SET_PLAYER_COLOR:
      return { ...state, playerColor: action.payload }
    case SET_SETUP_OPEN:
      return { ...state, setupOpen: action.payload }
    case RESET_CHESSBOARD:
      return initialState
    default:
      return state
  }
}

export const chessboardStore = createStore(chessboardReducer)

export const chessboardActions = {
  setFen: (fen) => ({ type: SET_FEN, payload: fen }),
  setStatus: (status) => ({ type: SET_STATUS, payload: status }),
  setIsThinking: (isThinking) => ({ type: SET_IS_THINKING, payload: isThinking }),
  setLastMove: (lastMove) => ({ type: SET_LAST_MOVE, payload: lastMove }),
  setRequestBody: (requestBody) => ({ type: SET_REQUEST_BODY, payload: requestBody }),
  setGameMode: (gameMode) => ({ type: SET_GAME_MODE, payload: gameMode }),
  setPlayerColor: (playerColor) => ({ type: SET_PLAYER_COLOR, payload: playerColor }),
  setSetupOpen: (setupOpen) => ({ type: SET_SETUP_OPEN, payload: setupOpen }),
  resetChessboard: () => ({ type: RESET_CHESSBOARD }),
}

export function useChessboardSelector(selector) {
  return useSyncExternalStore(
    chessboardStore.subscribe,
    () => selector(chessboardStore.getState()),
    () => selector(chessboardStore.getState()),
  )
}

export { INITIAL_FEN }
