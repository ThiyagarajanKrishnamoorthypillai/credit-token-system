import React, { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Container, Row, Col, Card, Button, Nav } from 'react-bootstrap'
import TokenMeter from './TokenMeter'
//import ActionTrigger from './ActionTrigger'
import CreditRechargeModal from './CreditRechargeModal'

const Dashboard = () => {
  const [email, setEmail] = useState('')
  const [credit, setCredit] = useState(0)
  const [googleId, setGoogleId] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    const savedEmail = localStorage.getItem('userEmail')
    const savedCredit = localStorage.getItem('credits')
    const savedId = localStorage.getItem('googleId')

    if (!savedEmail || !savedCredit || !savedId) {
      navigate('/')
    } else {
      setEmail(savedEmail)
      setCredit(Number(savedCredit))
      setGoogleId(savedId)
    }
  }, [navigate])

  const handleLogout = () => {
    localStorage.clear()
    navigate('/')
  }

  return (
    <Container fluid className="mt-4">
      <Row>
        {/* Sidebar Navigation */}
        <Col md={3} className="bg-light border-end p-3">
          <h5>📋 Menu</h5>
          <Nav className="flex-column">
            <Nav.Link as={Link} to="/dashboard">🏠 Home</Nav.Link>
            <Nav.Link as={Link} to="/image-generator">🧠 AI Image Generation</Nav.Link>
          </Nav>
          <Button variant="outline-danger" onClick={handleLogout} className="mt-4 w-100">
            Logout
          </Button>
        </Col>

        {/* Main Dashboard Content */}
        <Col md={9}>
          <Card className="shadow p-4">
            <h2 className="text-center mb-4">🎉 Welcome, {email}</h2>

            {/* ✅ Simplified credit text */}
            <h4 className="text-center text-primary">
              Current Credit: <strong>{credit}</strong>
            </h4>

            <TokenMeter credit={credit} />
            {/*<ActionTrigger googleId={googleId} onCreditUpdated={setCredit} />*/}
            <CreditRechargeModal googleId={googleId} onCreditUpdated={setCredit} />
          </Card>
        </Col>
      </Row>
    </Container>
  )
}

export default Dashboard
