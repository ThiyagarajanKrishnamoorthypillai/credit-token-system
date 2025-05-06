/*import React, { useState } from 'react'
import PropTypes from 'prop-types'
import axios from 'axios'
import { Button, Spinner, Alert } from 'react-bootstrap'


const ActionTrigger = ({ googleId, onCreditUpdated }) => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')

  const handleAction = async () => {
    const amount = 100

    setLoading(true)
    setError('')
    setSuccessMsg('')

    try {
      const res = await axios.post('http://localhost:8080/api/usage/deduct', {
        googleId,
        amount
      })

      const updatedCredit = res.data.remainingCredit
      onCreditUpdated(updatedCredit)
      localStorage.setItem('credit', updatedCredit)
      setSuccessMsg(`✅ Action successful! Remaining Credit: ${updatedCredit}`)
    } catch (err) {
      const msg = err.response?.data || 'An error occurred'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="my-4 text-center">
      <h5>Trigger Comic / AI Action</h5>

      <div className="d-flex justify-content-center mb-3">
        <Button
          variant="primary"
          onClick={handleAction}
          disabled={loading}
          className="px-4"
        >
          {loading
            ? <Spinner animation="border" size="sm" />
            : 'Use 100 Credits'
          }
        </Button>
      </div>

      {successMsg && (
        <Alert variant="success" dismissible onClose={() => setSuccessMsg('')}>
          {successMsg}
        </Alert>
      )}

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError('')}>
          {error}
        </Alert>
      )}
    </div>
  )
}

ActionTrigger.propTypes = {
  googleId: PropTypes.string.isRequired,
  onCreditUpdated: PropTypes.func.isRequired,
}

export default ActionTrigger
*/