# Trem-Tech POS — Pre-APK QA Checklist

## Critical workflows
- [ ] App launches without crash
- [ ] First-run admin login works
- [ ] Password change works
- [ ] Cashier/Manager/Admin role restrictions
- [ ] Add supplier and supplier details
- [ ] Add customer
- [ ] Add product
- [ ] Receive stock with date, cost, selling price, brand/model
- [ ] Serialized stock: IMEI/serial/colour
- [ ] Non-serialized stock does not require IMEI/serial
- [ ] Barcode/QR stored and searched
- [ ] Search stock by product, barcode, IMEI and serial
- [ ] Complete sale
- [ ] Cash / bank / mobile-money / card payment capture
- [ ] Receipt PDF
- [ ] Share receipt
- [ ] Credit purchase
- [ ] Layby
- [ ] Account payment
- [ ] Refund
- [ ] Stock adjustment
- [ ] Stock return
- [ ] Supplier payment/balance
- [ ] Customer statement PDF
- [ ] Gross-profit report
- [ ] Dashboard
- [ ] CSV/Excel export
- [ ] Verified backup
- [ ] Backup validation
- [ ] Restore + emergency pre-restore backup
- [ ] Location stock
- [ ] Location transfer
- [ ] Audit log

## Release blockers
1. Any crash in a critical workflow.
2. Stock quantity becomes incorrect after sale/refund/adjustment/transfer.
3. IMEI/serial can be duplicated unintentionally.
4. A lower-privilege user can perform an admin-only action.
5. Restore can overwrite data without creating an emergency backup.
6. Receipt totals differ from sale totals.
7. Credit/layby balances do not reconcile.

## Recommended device tests
- Small Android phone
- Mid-size Android phone
- Android 13+
- Android 14+
- Android 15+
