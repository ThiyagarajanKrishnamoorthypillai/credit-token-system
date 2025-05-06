import React, { useState, useEffect, useCallback } from 'react'
import axios from 'axios'
import { Container, Form, Button, Alert, Spinner, Card } from 'react-bootstrap'

const ImageGenerator = ({ googleId, onCreditsUpdated }) => {
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(false)
  const [images, setImages] = useState([])
  const [history, setHistory] = useState([])
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [credit, setCredit] = useState(0)

  const fetchHistory = useCallback(async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/images/history', {
        params: { googleId }
      })
      const unique = Array.from(
        new Map(res.data.map(item => [item.imageUrl, item])).values()
      )
      setHistory(unique)
    } catch (err) {
      console.error('History fetch failed', err)
    }
  }, [googleId])

  const fetchCredit = useCallback(async () => {
    // first try backend
    try {
      const res = await axios.get('http://localhost:8080/api/usage/credits', {
        params: { googleId }
      });
      setCredit(res.data.credit);
      localStorage.setItem('credit', res.data.credit);
      return;
    } catch (err) {
      console.warn('Backend credit fetch failed, falling back to localStorage');
    }
    // fallback to localStorage
    const saved = localStorage.getItem('credit');
    setCredit(saved ? Number(saved) : 0);
  }, [googleId]);
  

  useEffect(() => {
    if (googleId) {
      fetchCredit()
      fetchHistory()
    }
  }, [fetchCredit, fetchHistory, googleId])

  const handleGenerate = async () => {
    if (!keyword.trim()) return
    if (credit < 40) {
      setError('Not enough credits. Please upgrade your plan.')
      return
    }

    setLoading(true)
    setImages([])
    setError('')
    setMessage('')

    try {
      const response = await axios.post('http://localhost:8080/api/images/generate', null, {
        params: { googleId, prompt: keyword }
      })

      const { images: generated, remainingCredit } = response.data
      setImages(generated)
      setCredit(remainingCredit)
      onCreditsUpdated(remainingCredit)
      setMessage(`${generated.length} images generated!`)

      await fetchHistory()
    } catch (err) {
      setError(err.response?.data || 'Image generation failed')
    } finally {
      setLoading(false)
    }
  }

  const handleDownload = async (url) => {
    try {
      const response = await fetch(url)
      const blob = await response.blob()
      const blobUrl = window.URL.createObjectURL(blob)

      const link = document.createElement('a')
      link.href = blobUrl
      link.download = 'ai-image-' + Date.now() + '.jpg'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(blobUrl)
    } catch (error) {
      console.error('Download failed:', error)
    }
  }

  const handleUpgradeClick = () => {
    window.location.href = "https://razorpay.com/payment-link/your-upgrade-plan-url"
  }

  return (
    <Container className="mt-4">
      <Card className="p-4 shadow">
        <h4 className="text-center">🎨 AI Image Generator</h4>
        <p className="text-center text-muted">Available Credits: <strong>{credit}</strong></p>

        <Form className="my-3">
          <Form.Group controlId="keyword">
            <Form.Label>Enter prompt keyword</Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="e.g. Lord Shiva, mountain view"
              style={{ resize: 'vertical', width: '100%' }}
            />
          </Form.Group>
          <Button
            variant="primary"
            className="mt-3"
            onClick={handleGenerate}
            disabled={loading || credit < 40}
          >
            {loading ? <Spinner animation="border" size="sm" /> : 'Generate Images (Cost: 40 Tokens)'}
          </Button>
        </Form>

        {error && <Alert variant="danger">{error}</Alert>}
        {message && <Alert variant="success">{message}</Alert>}

        {images.length > 0 && (
          <div className="d-flex flex-wrap justify-content-center gap-3 mt-4">
            {images.map((url, i) => (
              <div key={i} className="position-relative text-center">
                <img
                  src={url}
                  alt="Generated"
                  style={{ width: 200, height: 200, borderRadius: 8, objectFit: 'cover' }}
                />
                <Button
                  variant="light"
                  className="position-absolute top-0 end-0 m-1"
                  onClick={() => handleDownload(url)}
                  style={{
                    backgroundColor: 'rgba(0,0,0,0.6)',
                    borderRadius: '50%',
                    border: 'none',
                    padding: '6px'
                  }}
                  onMouseOver={(e) => {
                    e.currentTarget.style.transform = 'scale(1.15)'
                    e.currentTarget.firstChild.style.color = '#ffffff'
                  }}
                  onMouseOut={(e) => {
                    e.currentTarget.style.transform = 'scale(1)'
                    e.currentTarget.firstChild.style.color = '#ccc'
                  }}
                  title="Download to device"
                >
                  <i className="fas fa-download" style={{ fontSize: '0.8rem', color: '#ccc' }}></i>
                </Button>
              </div>
            ))}
          </div>
        )}

        {history.length > 0 && (
          <div className="mt-5">
            <h5>Your Image History</h5>
            <div className="d-flex flex-wrap gap-3">
              {history.map((entry, index) => (
                <div key={index} className="text-center position-relative">
                  <img
                    src={entry.imageUrl}
                    alt={`Generated ${entry.timestamp}`}
                    style={{ width: 150, height: 150, borderRadius: 8, objectFit: 'cover' }}
                  />
                  <Button
                    variant="light"
                    className="position-absolute top-0 end-0 m-1"
                    onClick={() => handleDownload(entry.imageUrl)}
                    style={{
                      backgroundColor: 'rgba(0,0,0,0.6)',
                      borderRadius: '50%',
                      border: 'none',
                      padding: '6px'
                    }}
                    onMouseOver={(e) => {
                      e.currentTarget.style.transform = 'scale(1.15)'
                      e.currentTarget.firstChild.style.color = '#ffffff'
                    }}
                    onMouseOut={(e) => {
                      e.currentTarget.style.transform = 'scale(1)'
                      e.currentTarget.firstChild.style.color = '#ccc'
                    }}
                    title="Download to device"
                  >
                    <i className="fas fa-download" style={{ fontSize: '0.8rem', color: '#ccc' }}></i>
                  </Button>
                  <p style={{ fontSize: '12px' }}>{new Date(entry.timestamp).toLocaleString()}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {credit <= 0 && (
          <Alert variant="warning" className="mt-5 text-center">
            <p>You are currently on a free plan with no remaining credits.</p>
            <Button variant="success" onClick={handleUpgradeClick}>
              Upgrade Plan
            </Button>
          </Alert>
        )}
      </Card>
    </Container>
  )
}

export default ImageGenerator
