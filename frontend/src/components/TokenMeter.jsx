import React from 'react'
import { ProgressBar } from 'react-bootstrap'

const TokenMeter = ({ credit }) => {
  const maxTokens = 1500
  const percentage = (credit / maxTokens) * 100

  let variant = 'success'
  if (percentage <= 50) variant = 'warning'
  if (percentage <= 20) variant = 'danger'

  return (
    <div className="my-4">
      <h5>Credit Balance</h5>
      <ProgressBar
        now={percentage}
        label={`${credit}`}
        variant={variant}
        striped
        animated
      />
    </div>
  )
}

export default TokenMeter
