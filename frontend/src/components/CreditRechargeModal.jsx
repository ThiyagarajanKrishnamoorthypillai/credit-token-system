import React, { useState } from 'react'
import { Modal, Button, Form, Alert } from 'react-bootstrap'
import axios from 'axios'

const CreditRechargeModal = ({ googleId, onCreditUpdated }) => {
  const [show, setShow] = useState(false)
  const [amount, setAmount] = useState(500)
  const [loading, setLoading] = useState(false)
  const [successMsg, setSuccessMsg] = useState('')
  const [errorMsg, setErrorMsg] = useState('')

  const handleRecharge = async () => {
    setLoading(true)
    setSuccessMsg('')
    setErrorMsg('')

    try {
      const response = await axios.post('http://localhost:8080/api/billing/recharge', {
        googleId,
        amount
      })

      const updatedCredit = response.data.newBalance
      localStorage.setItem('credit', updatedCredit)
      onCreditUpdated(updatedCredit)
      setSuccessMsg(`Recharged! New Balance: ${updatedCredit}`)
      setShow(false)
    } catch (error) {
      setErrorMsg(error.response?.data || 'Recharge failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Button variant="success" onClick={() => setShow(true)} className="mt-3">
        Recharge Credit
      </Button>

      <Modal show={show} onHide={() => setShow(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Recharge Credit</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {successMsg && <Alert variant="success">{successMsg}</Alert>}
          {errorMsg && <Alert variant="danger">{errorMsg}</Alert>}
          <Form.Group>
            <Form.Label>Select Credit Package</Form.Label>
            <Form.Select
              value={amount}
              onChange={(e) => setAmount(Number(e.target.value))}
            >
              <option value={500}>+500 Credits</option>
              <option value={1000}>+1000 Credits</option>
              <option value={1500}>+1500 Credits</option>
            </Form.Select>
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShow(false)}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleRecharge} disabled={loading}>
            {loading ? 'Processing...' : 'Recharge'}
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  )
}

export default CreditRechargeModal
