import React from 'react'
import { GoogleLogin } from '@react-oauth/google'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

const LoginWithGoogle = () => {
  const navigate = useNavigate()

  const handleLoginSuccess = async (credentialResponse) => {
    const token = credentialResponse.credential

    try {
      const response = await axios.post('http://localhost:8080/api/auth/google-login', { token }, {
        headers: { 'X-Forwarded-For': '127.0.0.1' },
      })

      const { email, credits, googleId } = response.data
      localStorage.setItem('userEmail', email)
      localStorage.setItem('credits', credits)
      localStorage.setItem('googleId', googleId)

      navigate('/dashboard')
    } catch (error) {
      console.error('Login error:', error)
      alert('Google login failed or unauthorized user')
    }
  }

  return (
    <div className="container text-center mt-5">
      <h2>Login with Google to Continue</h2>
      <GoogleLogin
        onSuccess={handleLoginSuccess}
        onError={() => alert('Google Login Failed')}
        useOneTap
      />
    </div>
  )
}

export default LoginWithGoogle
