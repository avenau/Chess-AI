import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Typography,
} from '@mui/material'

const PROMOTION_OPTIONS = [
  { value: 'q', label: 'Queen' },
  { value: 'r', label: 'Rook' },
  { value: 'b', label: 'Bishop' },
  { value: 'n', label: 'Knight' },
]

export default function ChessPromotionModal({
  open,
  onClose,
  onSelect,
}) {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>Choose a promotion piece</DialogTitle>
      <DialogContent>
        <Stack spacing={1.5} sx={{ pt: 1 }}>
          <Typography variant="body2" sx={{ color: '#5a4736' }}>
            Your pawn reached the final rank. Select the piece to promote to.
          </Typography>
          {PROMOTION_OPTIONS.map((option) => (
            <Button
              key={option.value}
              variant="outlined"
              onClick={() => onSelect(option.value)}
              sx={{
                minHeight: 48,
                borderRadius: 2,
                justifyContent: 'flex-start',
                borderColor: 'rgba(46, 36, 25, 0.16)',
                color: '#2e2419',
              }}
            >
              {option.label}
            </Button>
          ))}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 3 }}>
        <Button onClick={onClose} sx={{ color: '#5a4736' }}>
          Cancel
        </Button>
      </DialogActions>
    </Dialog>
  )
}
