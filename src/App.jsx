import { useState } from 'react'
import { Box, Container, FormControl, InputLabel, MenuItem, TextField, Typography ,Select, CircularProgress ,Button ,Alert,Snackbar} from '@mui/material';
import './App.css'
import axios from 'axios';

function App() {
  const [emailContent,setemailContent] = useState('');
  const [tone,setTone] = useState('');
  const [generatedReply,setgeneratedReply] = useState('');
  const [loading , setLoading] = useState(false);
  const [error , setError] = useState('');
  const [snackbarOpen,setSnackbarOpen] = useState(false);

  const handleSubmit = async ()=>{
    setLoading(true);
    setError('');
    try{
      const response = await axios.post("http://localhost:8080/api/email/generate",{
        emailContent,
        tone
      });
      setgeneratedReply(typeof response.data==='string' ? response.data : JSON.stringify(response.data))
    }
    catch(error){
      setError('Failed to generate email reply')
      console.log(error)
    }
    finally{
      setLoading(false);
    }
  }

  const handleCopy = ()=>{
    navigator.clipboard.writeText(generatedReply);
    setSnackbarOpen(true);
  }

  return (
    <Container maxWidth="md" sx={{py:4}}>

      <Box sx={{ mx: 3 , py:2 }}>
        <TextField 
          fullWidth
          multiline
          rows={6}
          variant='outlined'
          label="Original Email Content"
          value={emailContent || ''}
          onChange={(e)=>setemailContent(e.target.value)}
          sx={{ mb:3 }}/>

          <FormControl fullWidth sx={{mb:2}}>
            <InputLabel>Tone (Optional)</InputLabel>
            <Select value={tone || ''} label={"Tone (Optional)"} onChange={(e)=>setTone(e.target.value)}>
              <MenuItem value="">None</MenuItem>
              <MenuItem value="professional">Professional</MenuItem>
              <MenuItem value="casual">Casual</MenuItem>
              <MenuItem value="friendly">Friendly</MenuItem>
            </Select>
          </FormControl>

          <Button variant='contained' onClick={handleSubmit} disabled={!emailContent || loading} fullWidth>
            {loading ? <CircularProgress size={24}/> : "Generate Reply"}
          </Button>
      </Box>

      {error && (
        <Typography color='error' sx={{ mb:2 }}>
          {error}
        </Typography>
      )}

      {generatedReply && (
        <Box sx={{ mt:3 }}>
          <Typography variant='h6' gutterBottom>
            Generated Reply : 
          </Typography>

          <TextField fullWidth multiline rows={6} variant='outlined' value={generatedReply || ""}/>

          <Button variant='outlined' sx={{ mt:2 }} onClick={handleCopy}>Copy to Clipboard</Button>
        </Box>
      )}

      <Snackbar 
        open={snackbarOpen} 
        autoHideDuration={2000} 
        onClose={()=>setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={()=>setSnackbarOpen(false)} severity="success" sx={{ width: '100%' }}>
          Copied to clipboard!
        </Alert>
      </Snackbar>

    </Container>
  )
}

export default App
