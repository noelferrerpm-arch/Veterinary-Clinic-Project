package cat.tecnocampus.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity(name="Payment")
@Table(name="payment")
public class Payment {
    @Id
    @GeneratedValue
    private Long paymentId;

    @Column(name="payment_date")
    private LocalDate paymentDate;

    @Column(name="amount")
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(name="payment_method")
    private PaymentMethod paymentMethod;

    //@GeneratedValue
    @Column(name="transaction_ref", unique = true)
    private Long transactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;



    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(long transactionRef) {
        this.transactionRef = transactionRef;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
