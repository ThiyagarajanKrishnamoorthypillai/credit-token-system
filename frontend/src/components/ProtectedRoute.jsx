import React from 'react'
import { Navigate } from 'react-router-dom'

const ProtectedRoute = ({ children }) => {
  const googleId = localStorage.getItem('googleId')
  if (!googleId) {
    // Not authenticated → redirect to login
    return <Navigate to="/" replace />
  }
  return children
}

export default ProtectedRoute
