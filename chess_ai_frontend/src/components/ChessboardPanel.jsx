import { useEffect, useRef, useState } from 'react'
import axios from 'axios'
import { Chess } from 'chess.js'
import { Chessboard } from 'react-chessboard'
import {
  Button,
  Grid,
  Paper,
  Stack,
  Typography,
} from '@mui/material'
import {
  chessboardActions,
  chessboardStore,
  INITIAL_FEN,
  useChessboardSelector,
} from '../state/ChessboardState'
import ChessGameSetupModal from './ChessGameSetupModal'

const CHESS_API_BASE_URL = import.meta.env.VITE_CHESS_API_BASE_URL ?? 'http://localhost:8080'

function safeGameFromFen(fen) {
  return new Chess(fen)
}

function extractNextFen(payload, currentFen) {
  const targetSquare = payload?.nextMove?.target
  const fromSquare = payload?.nextMove?.from;
  const promotion = payload?.nextMove?.promotion

  console.log(targetSquare);
  console.log(promotion);

  if (!targetSquare) {
    return null
  }

  const game = safeGameFromFen(currentFen)
  //const moves = game.moves({ verbose: true })
  // const matchingMove = moves.find(
  //   (move) => move.to === targetSquare && move.from === fromSquare && (promotion === "NONE" || move.promotion === promotion),
  // )

  // if (!matchingMove) {
  //   return null
  // }
    console.log(game.moves({ verbose: true }));

    const convertedPromotion = promotion === "NONE" ? undefined : promotion;
  const result = game.move({
    from: fromSquare,
    to: targetSquare,
    promotion: convertedPromotion,
  })

  console.log("FULL LOGS");
  console.log(fromSquare);
  console.log(targetSquare);
  console.log(promotion);
  console.log(result);
  // console.log(game.moves({ verbose: true }));

  return result
    ? {
        fen: game.fen(),
        move: result,
      }
    : null
}

function requestNextMove({ fen, automatic = false }) {
  chessboardStore.dispatch(chessboardActions.setIsThinking(true))
  chessboardStore.dispatch(
    chessboardActions.setStatus(
      automatic
        ? `Requesting bot move from ${CHESS_API_BASE_URL}/api/chess/best-move...`
        : `Requesting next move from ${CHESS_API_BASE_URL}/api/chess/best-move...`,
    ),
  )
  console.log("TESTING ");
  console.log(CHESS_API_BASE_URL);
  try {
    axios.post(
      `${CHESS_API_BASE_URL}/api/chess/best-move`,
      { fen },
      {
        headers: {
          'Content-Type': 'application/json',
        },
      },
    ).then(r => {
      const payload = r.data
      const nextMoveResult = extractNextFen(payload, fen)
      console.log("LOG ");
      console.log(nextMoveResult);
      if (!nextMoveResult) {
        chessboardStore.dispatch(
          chessboardActions.setStatus(
            'Received a response, but it did not contain a usable move or FEN yet.',
          ),
        )
        return
      }

      chessboardStore.dispatch(chessboardActions.setFen(nextMoveResult.fen))
      chessboardStore.dispatch(
        chessboardActions.setLastMove(`${nextMoveResult.move.from}-${nextMoveResult.move.to}`),
      )
      chessboardStore.dispatch(
        chessboardActions.setRequestBody(JSON.stringify({ fen: nextMoveResult.fen }, null, 2)),
      )
      chessboardStore.dispatch(
        chessboardActions.setStatus(
          automatic
            ? `Bot moved ${nextMoveResult.move.piece.toUpperCase()} from ${nextMoveResult.move.from} to ${nextMoveResult.move.to}.`
            : 'Applied the backend move to the board.',
        ),
      )
    })


  } catch (error) {
    if (axios.isAxiosError(error)) {
      chessboardStore.dispatch(
        chessboardActions.setStatus(
          error.response?.data?.error ?? error.message ?? 'Request failed.',
        ),
      )
    } else {
      chessboardStore.dispatch(
        chessboardActions.setStatus(
          error instanceof Error ? error.message : 'Request failed.',
        ),
      )
    }
  } finally {
    chessboardStore.dispatch(chessboardActions.setIsThinking(false))
  }
}

export default function ChessboardPanel() {
  const {
    fen,
    status,
    isThinking,
    lastMove,
    requestBody,
    gameMode,
    playerColor,
    setupOpen,
  } = useChessboardSelector(
    (state) => state,
  )
  const autoRequestedFenRef = useRef(null)
  const [selectedSquare, setSelectedSquare] = useState(null)
  const turn = fen.split(' ')[1]
  const turnColor = turn === 'w' ? 'white' : 'black'
  const isPlayersTurn = gameMode === 'bot' ? playerColor === turnColor : true
  const boardOrientation = gameMode === 'two-player' ? turnColor : playerColor
  const selectedSquareMoves = selectedSquare
    ? safeGameFromFen(fen).moves({ square: selectedSquare, verbose: true })
    : []
  const customSquareStyles = selectedSquare
    ? selectedSquareMoves.reduce(
        (styles, move) => {
          styles[move.to] = move.captured
            ? {
                background:
                  'radial-gradient(circle, rgba(186, 58, 44, 0.22) 0%, rgba(186, 58, 44, 0.22) 58%, rgba(186, 58, 44, 0.72) 58%, rgba(186, 58, 44, 0.72) 72%, transparent 72%)',
              }
            : {
                background:
                  'radial-gradient(circle, rgba(31, 91, 77, 0.34) 0%, rgba(31, 91, 77, 0.34) 22%, transparent 24%)',
              }
          return styles
        },
        {
          [selectedSquare]: {
            backgroundColor: 'rgba(214, 178, 66, 0.45)',
          },
        },
      )
    : {}

  function canControlPiece(piece) {
    if (setupOpen || isThinking || !piece) {
      return false
    }

    const pieceColor = piece.startsWith('w') ? 'white' : 'black'

    if (gameMode === 'bot') {
      return pieceColor === playerColor && pieceColor === turnColor
    }

    return pieceColor === turnColor
  }

  function applyMove(sourceSquare, targetSquare) {
    if (!targetSquare || !isPlayersTurn) {
      return false
    }

    const nextGame = safeGameFromFen(fen)
    const move = nextGame.move({
      from: sourceSquare,
      to: targetSquare,
      promotion: 'q',
    })

    if (!move) {
      chessboardStore.dispatch(
        chessboardActions.setStatus('Illegal move. Try a legal move on the board.'),
      )
      return false
    }

    const nextFen = nextGame.fen()
    chessboardStore.dispatch(chessboardActions.setFen(nextFen))
    chessboardStore.dispatch(chessboardActions.setLastMove(`${move.from}-${move.to}`))
    chessboardStore.dispatch(
      chessboardActions.setRequestBody(JSON.stringify({ fen: nextFen }, null, 2)),
    )
    chessboardStore.dispatch(
      chessboardActions.setStatus(
        `Moved ${move.piece.toUpperCase()} from ${move.from} to ${move.to}.`,
      ),
    )
    setSelectedSquare(null)

    return true
  }

  const boardOptions = {
    position: fen.split(' ')[0],
    boardOrientation,
    allowDragging: !setupOpen && !isThinking,
    customSquareStyles,
    canDragPiece: ({ piece }) => {
      return canControlPiece(piece?.pieceType)
    },
    onPieceDrop: ({ sourceSquare, targetSquare }) => {
      return applyMove(sourceSquare, targetSquare)
    },
    onSquareClick: (squareOrEvent) => {
      const square =
        typeof squareOrEvent === 'string' ? squareOrEvent : squareOrEvent?.square

      if (!square || setupOpen || isThinking || !isPlayersTurn) {
        return
      }

      const game = safeGameFromFen(fen)
      const piece = game.get(square)
      const pieceCode = piece ? `${piece.color}${piece.type.toUpperCase()}`.toLowerCase() : null

      if (!selectedSquare) {
        if (!canControlPiece(pieceCode)) {
          return
        }

        setSelectedSquare(square)
        chessboardStore.dispatch(
          chessboardActions.setStatus(`Selected ${square}. Choose a destination square.`),
        )
        return
      }

      if (selectedSquare === square) {
        setSelectedSquare(null)
        chessboardStore.dispatch(chessboardActions.setStatus(`Selection cleared from ${square}.`))
        return
      }

      if (canControlPiece(pieceCode)) {
        setSelectedSquare(square)
        chessboardStore.dispatch(
          chessboardActions.setStatus(`Selected ${square}. Choose a destination square.`),
        )
        return
      }

      applyMove(selectedSquare, square)
    },
  }

  useEffect(() => {
    setSelectedSquare(null)
  }, [fen, setupOpen])

  useEffect(() => {
    if (setupOpen) {
      autoRequestedFenRef.current = null
      return
    }

    if (gameMode !== 'bot' || isThinking || isPlayersTurn) {
      return
    }

    if (autoRequestedFenRef.current === fen) {
      return
    }

    autoRequestedFenRef.current = fen
    requestNextMove({fen, automatic: true});
   }, [fen, gameMode, isPlayersTurn, isThinking, setupOpen])

  function resetBoard() {
    autoRequestedFenRef.current = null
    chessboardStore.dispatch(chessboardActions.resetChessboard())
  }

  function startGame() {
    if (!gameMode) {
      return
    }

    autoRequestedFenRef.current = null
    chessboardStore.dispatch(chessboardActions.resetChessboard())
    chessboardStore.dispatch(chessboardActions.setGameMode(gameMode))
    chessboardStore.dispatch(chessboardActions.setPlayerColor(playerColor))
    chessboardStore.dispatch(chessboardActions.setSetupOpen(false))
    chessboardStore.dispatch(
      chessboardActions.setStatus(
        gameMode === 'bot'
          ? `Game started. You are playing as ${playerColor}.`
          : 'Two-player game started.',
      ),
    )
  }

  return (
    <>
      <ChessGameSetupModal
        open={setupOpen}
        gameMode={gameMode}
        playerColor={playerColor}
        onGameModeChange={(value) => {
          chessboardStore.dispatch(chessboardActions.setGameMode(value))
        }}
        onPlayerColorChange={(value) => {
          chessboardStore.dispatch(chessboardActions.setPlayerColor(value))
        }}
        onStart={startGame}
      />

      <Grid container spacing={{ xs: 2, md: 3 }}>
        <Grid size={12}>
        <Paper
          elevation={0}
          sx={{
            p: { xs: 2, md: 3 },
            borderRadius: 2,
            border: '1px solid',
            borderColor: 'rgba(66, 44, 21, 0.12)',
            backgroundColor: 'rgba(255, 250, 244, 0.76)',
            backdropFilter: 'blur(16px)',
          }}
        >
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 'auto' }}>
              <Button
                fullWidth
                variant="contained"
                onClick={() => requestNextMove({ fen })}
                disabled={isThinking || setupOpen || (gameMode === 'bot' && isPlayersTurn)}
                sx={{
                  minHeight: 52,
                  px: 3,
                  borderRadius: 2,
                  bgcolor: '#1f5b4d',
                  '&:hover': { bgcolor: '#17463b' },
                }}
              >
                {isThinking ? 'Waiting for move...' : 'Get Next Move'}
              </Button>
            </Grid>
            <Grid size={{ xs: 12, sm: 'auto' }}>
              <Button
                fullWidth
                variant="outlined"
                onClick={resetBoard}
                sx={{
                  minHeight: 52,
                  px: 3,
                  borderRadius: 2,
                  borderColor: 'rgba(46, 36, 25, 0.16)',
                  color: '#2e2419',
                }}
              >
                Reset Board
              </Button>
            </Grid>
          </Grid>
        </Paper>
        </Grid>

        <Grid size={{ xs: 12, lg: 7 }}>
        <Paper
          elevation={0}
          sx={{
            p: { xs: 2, md: 3 },
            borderRadius: 2.5,
            border: '1px solid',
            borderColor: 'rgba(66, 44, 21, 0.12)',
            backgroundColor: 'rgba(255, 250, 244, 0.76)',
            backdropFilter: 'blur(16px)',
            height: '100%',
          }}
        >
          <Stack alignItems="center">
            <Stack sx={{ width: '100%', maxWidth: 620 }}>
              <Chessboard options={boardOptions} />
            </Stack>
          </Stack>
        </Paper>
        </Grid>

        <Grid size={{ xs: 12, lg: 5 }}>
        <Grid container spacing={2}>
          <Grid size={12}>
            <Paper
              elevation={0}
              sx={{
                p: 3,
                borderRadius: 2,
                border: '1px solid',
                borderColor: 'rgba(66, 44, 21, 0.12)',
                backgroundColor: 'rgba(255, 250, 244, 0.76)',
              }}
            >
              <Stack spacing={1.5}>
                <Typography variant="overline" sx={{ color: '#8b5e34', fontWeight: 700 }}>
                  Current FEN
                </Typography>
                <Typography
                  variant="body2"
                  sx={{
                    fontFamily: '"SFMono-Regular", Consolas, "Liberation Mono", monospace',
                    color: '#32261c',
                    overflowX: 'auto',
                  }}
                >
                  {fen}
                </Typography>
              </Stack>
            </Paper>
          </Grid>

          <Grid size={12}>
            <Paper
              elevation={0}
              sx={{
                p: 3,
                borderRadius: 2,
                border: '1px solid',
                borderColor: 'rgba(66, 44, 21, 0.12)',
                backgroundColor: 'rgba(255, 250, 244, 0.76)',
              }}
            >
              <Stack spacing={1.5}>
                <Typography variant="overline" sx={{ color: '#8b5e34', fontWeight: 700 }}>
                  Request Payload
                </Typography>
                <Typography
                  component="pre"
                  variant="body2"
                  sx={{
                    m: 0,
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                    fontFamily: '"SFMono-Regular", Consolas, "Liberation Mono", monospace',
                    color: '#32261c',
                  }}
                >
                  {requestBody || JSON.stringify({ fen }, null, 2)}
                </Typography>
              </Stack>
            </Paper>
          </Grid>

          <Grid size={12}>
            <Paper
              elevation={0}
              sx={{
                p: 3,
                borderRadius: 2,
                border: '1px solid',
                borderColor: 'rgba(66, 44, 21, 0.12)',
                backgroundColor: 'rgba(255, 250, 244, 0.76)',
              }}
            >
              <Stack spacing={1.5}>
                <Typography variant="overline" sx={{ color: '#8b5e34', fontWeight: 700 }}>
                  Status
                </Typography>
                <Typography variant="body1" sx={{ color: '#32261c' }}>
                  {status}
                </Typography>
                <Typography variant="body2" sx={{ color: '#7a6552' }}>
                  Mode: {gameMode === 'bot' ? `Bot game, you are ${playerColor}` : gameMode === 'two-player' ? 'Two-player game' : 'Not started'}
                </Typography>
                {lastMove ? (
                  <Typography variant="body2" sx={{ color: '#7a6552' }}>
                    Last local move: {lastMove}
                  </Typography>
                ) : null}
              </Stack>
            </Paper>
          </Grid>
        </Grid>
        </Grid>
      </Grid>
    </>
  )
}
