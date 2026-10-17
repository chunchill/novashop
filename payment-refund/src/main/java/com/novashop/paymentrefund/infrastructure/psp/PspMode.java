package com.novashop.paymentrefund.infrastructure.psp;

public enum PspMode {
  /** Immediate sync success from the stub PSP. */
  SUCCESS_SYNC,
  /** Immediate sync failure from the stub PSP. */
  FAIL,
  /** Leave refund IN_PROGRESS; complete via POST /psp/callbacks/refunds. */
  CALLBACK_LATER
}
