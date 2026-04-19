import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import ChessPage from './components/ChessPage'

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1f5b4d',
    },
    background: {
      default: '#efe6d7',
      paper: '#fffaf4',
    },
    text: {
      primary: '#21170f',
      secondary: '#4f4337',
    },
  },
  shape: {
    borderRadius: 10,
  },
  typography: {
    fontFamily: 'Georgia, "Times New Roman", serif',
    h2: {
      fontWeight: 700,
    },
  },
})

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <ChessPage />
    </ThemeProvider>
  )
}

export default App
