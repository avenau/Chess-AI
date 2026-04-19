import { Box, Chip, Container, Grid, Paper, Stack, Typography } from '@mui/material'
import ChessboardPanel from './ChessboardPanel'

export default function ChessPage() {
  return (
    <Box
      sx={{
        minHeight: '100svh',
        py: { xs: 4, md: 6 },
        background:
          'radial-gradient(circle at top left, rgba(194, 166, 106, 0.24), transparent 28%), radial-gradient(circle at bottom right, rgba(124, 58, 36, 0.18), transparent 32%), linear-gradient(180deg, #f8f1e5 0%, #efe6d7 100%)',
      }}
    >
      <Container maxWidth="xl">
        <Grid container spacing={{ xs: 3, md: 4 }}>
          <Grid size={12}>
            <Paper
              elevation={0}
              sx={{
                p: { xs: 3, md: 4 },
                borderRadius: 2.5,
                border: '1px solid',
                borderColor: 'rgba(66, 44, 21, 0.12)',
                backgroundColor: 'rgba(255, 250, 244, 0.82)',
                backdropFilter: 'blur(16px)',
              }}
            >
              <Stack spacing={2}>
                <Chip
                  label="Chess AI Frontend"
                  sx={{
                    width: 'fit-content',
                    fontWeight: 700,
                    letterSpacing: '0.12em',
                    textTransform: 'uppercase',
                    bgcolor: 'rgba(139, 94, 52, 0.12)',
                    color: '#8b5e34',
                  }}
                />
                <Typography
                  variant="h2"
                  sx={{
                    fontSize: { xs: '2.2rem', md: '3.25rem' },
                    lineHeight: 1.05,
                    fontWeight: 700,
                    color: '#21170f',
                    maxWidth: 760,
                  }}
                >
                  Board wired for backend move requests.
                </Typography>
                <Typography variant="body1" sx={{ maxWidth: 760, color: '#4f4337' }}>
                  The board state is maintained as FEN, local moves are validated with
                  {' '}
                  <Box component="code">chess.js</Box>
                  {' '}
                  and backend requests post to
                  {' '}
                  <Box component="code">http://localhost:8080/api/chess/best-move</Box>
                  .
                </Typography>
              </Stack>
            </Paper>
          </Grid>

          <Grid size={12}>
            <ChessboardPanel />
          </Grid>
        </Grid>
      </Container>
    </Box>
  )
}
