import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { GoogleOAuthProvider } from '@react-oauth/google'
import LoginWithGoogle from './components/LoginWithGoogle'
import Dashboard from './components/Dashboard'
import ProtectedRoute from './components/ProtectedRoute'
import ImageGenerator from './components/ImageGenerator'
import 'font-awesome/css/font-awesome.min.css';

function App() {
  return (
    <GoogleOAuthProvider clientId="116163448492-cmb3n7u2iabknur3a0bomp16ndkshfb6.apps.googleusercontent.com">
      <Router>
        <Routes>
          {/* Public login route */}
          <Route path="/" element={<LoginWithGoogle />} />

          {/* Protected dashboard route */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          {/* ✅ New route for AI Image Generator */}
          <Route
            path="/image-generator"
            element={
              <ProtectedRoute>
                <ImageGenerator
                  googleId={localStorage.getItem('googleId')}
                  onCreditsUpdated={(credits) => localStorage.setItem('credits', credits)}
                />
              </ProtectedRoute>
            }
          />
<Route
  path="/generate"
  element={
    <ProtectedRoute>
      <ImageGenerator />
    </ProtectedRoute>
  }
/>
{/*
<Route
  path="/image-generator"
  element={
    <ProtectedRoute>
      <ImageGenerator />
    </ProtectedRoute>
  }
/>
*/}
          {/* Catch-all route */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </GoogleOAuthProvider>
  )
}

export default App
