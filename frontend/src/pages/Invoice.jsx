// src/pages/Invoice.jsx
import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

export default function Invoice() {
  const { orderId } = useParams();
  const navigate = useNavigate();

  const [invoice, setInvoice] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  console.log(">>> INVOICE PAGE MOUNTED, orderId =", orderId);

  useEffect(() => {
    if (!orderId) return;

    const loadInvoice = async () => {
      try {
        setLoading(true);
        setError(null);

        console.log(">>> Fetching invoice for orderId:", orderId);

        const res = await fetch(`/api/invoices/${orderId}`);
        const contentType = res.headers.get("content-type") || "";

        // Backend 404 / 500 vs dönerse
        if (!res.ok) {
          console.error(
            "Invoice fetch failed with status:",
            res.status,
            res.statusText
          );
          setError(
            `Invoice could not be loaded (status ${res.status}). ` +
              "Invoice endpoint might not be implemented yet."
          );
          return;
        }

        // Eğer cevap JSON değilse json() çağırma
        if (!contentType.includes("application/json")) {
          console.error(
            "Invoice endpoint did not return JSON. content-type =",
            contentType
          );
          setError("Invoice endpoint did not return JSON.");
          return;
        }

        const data = await res.json();
        console.log(">>> Invoice data:", data);
        setInvoice(data);
      } catch (err) {
        console.error("Failed to load invoice:", err);
        setError("Unexpected error while loading invoice.");
      } finally {
        setLoading(false);
      }
    };

    loadInvoice();
  }, [orderId]);

  return (
    <div
      style={{
        minHeight: "100vh",
        padding: "3rem",
        background: "#f5f5f5",
        fontFamily: "sans-serif",
      }}
    >
      <h1 style={{ marginBottom: "1rem" }}>INVOICE PAGE</h1>
      <p style={{ marginBottom: "1.5rem" }}>
        Order ID from URL: <strong>{orderId}</strong>
      </p>

      {loading && <p>Loading invoice...</p>}

      {error && (
        <p style={{ color: "red", marginBottom: "1.5rem" }}>
          {error}
        </p>
      )}

      {invoice && (
        <div
          style={{
            background: "white",
            padding: "1.5rem",
            borderRadius: "8px",
            boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
            maxWidth: "600px",
          }}
        >
          <h2 style={{ marginTop: 0 }}>Invoice Summary</h2>
          <p>
            <strong>Customer:</strong> {invoice.userName} (
            {invoice.userEmail})
          </p>
          <p>
            <strong>Subtotal:</strong> {invoice.subtotal}
          </p>
          <p>
            <strong>Shipping:</strong> {invoice.shipping}
          </p>
          <p>
            <strong>Total:</strong> {invoice.grandTotal}
          </p>

          {invoice.items && invoice.items.length > 0 && (
            <table
              style={{
                marginTop: "1rem",
                width: "100%",
                borderCollapse: "collapse",
                fontSize: "0.9rem",
              }}
            >
              <thead>
                <tr>
                  <th
                    style={{
                      textAlign: "left",
                      borderBottom: "1px solid #ddd",
                      paddingBottom: "0.5rem",
                    }}
                  >
                    Product
                  </th>
                  <th
                    style={{
                      textAlign: "right",
                      borderBottom: "1px solid #ddd",
                      paddingBottom: "0.5rem",
                    }}
                  >
                    Qty
                  </th>
                  <th
                    style={{
                      textAlign: "right",
                      borderBottom: "1px solid #ddd",
                      paddingBottom: "0.5rem",
                    }}
                  >
                    Line Total
                  </th>
                </tr>
              </thead>
              <tbody>
                {invoice.items.map((it, idx) => (
                  <tr key={`${it.productId}-${it.sku}-${idx}`}>
                    <td style={{ padding: "0.4rem 0" }}>
                      {it.productName} ({it.sku})
                    </td>
                    <td style={{ textAlign: "right" }}>{it.quantity}</td>
                    <td style={{ textAlign: "right" }}>{it.lineTotal}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      <button
        onClick={() => navigate("/home")}
        style={{
          marginTop: "2rem",
          padding: "0.75rem 1.5rem",
          border: "none",
          background: "#3d211c",
          color: "white",
          cursor: "pointer",
          fontWeight: 600,
        }}
      >
        Back to Home
      </button>
    </div>
  );
}
