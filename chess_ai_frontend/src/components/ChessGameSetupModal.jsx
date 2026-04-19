import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup,
  Stack,
  Typography,
} from '@mui/material'

export default function ChessGameSetupModal({
  open,
  gameMode,
  playerColor,
  onGameModeChange,
  onPlayerColorChange,
  onStart,
}) {
  return (
    <Dialog open={open} disableEscapeKeyDown fullWidth maxWidth="sm">
      <DialogTitle>Start a new game</DialogTitle>
      <DialogContent>
        <Stack spacing={3} sx={{ pt: 1 }}>
          <FormControl>
            <Typography variant="overline" sx={{ color: '#8b5e34', fontWeight: 700, mb: 1 }}>
              Game Mode
            </Typography>
            <RadioGroup value={gameMode ?? ''} onChange={(event) => onGameModeChange(event.target.value)}>
              <FormControlLabel value="two-player" control={<Radio />} label="Two player" />
              <FormControlLabel value="bot" control={<Radio />} label="Play vs bot" />
            </RadioGroup>
          </FormControl>

          {gameMode === 'bot' ? (
            <FormControl>
              <Typography variant="overline" sx={{ color: '#8b5e34', fontWeight: 700, mb: 1 }}>
                Your Side
              </Typography>
              <RadioGroup value={playerColor} onChange={(event) => onPlayerColorChange(event.target.value)}>
                <FormControlLabel value="white" control={<Radio />} label="Play as white" />
                <FormControlLabel value="black" control={<Radio />} label="Play as black" />
              </RadioGroup>
            </FormControl>
          ) : null}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 3 }}>
        <Button
          variant="contained"
          onClick={onStart}
          disabled={!gameMode}
          sx={{ borderRadius: 2, bgcolor: '#1f5b4d', '&:hover': { bgcolor: '#17463b' } }}
        >
          Start Game
        </Button>
      </DialogActions>
    </Dialog>
  )
}
