import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { meRequest } from "../lib/api";

export default function RequireAuth({ children }) {
  const [checking, setChecking] = useState(true);
  const [ok, setOk] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    let cancelled = false;

    async function check() {
      try {
        console.log(">>> RequireAuth: checking for", location.pathname);
        const res = await meRequest();
        if (!cancelled) {
          console.log("RequireAuth OK user:", res.data);
          setOk(true);
        }
      } catch (err) {
        console.log("RequireAuth: NOT authed, go /login");
        if (!cancelled) {
          navigate("/login", {
            replace: true,
            state: { from: location },
          });
        }
      } finally {
        if (!cancelled) setChecking(false);
      }
    }

    check();
    return () => {
      cancelled = true;
    };
  }, [navigate, location]);

  if (checking) {
    return (
      <div className="home-page">
        <div style={{ padding: "2rem", textAlign: "center" }}>
          Checking session...
        </div>
      </div>
    );
  }

  if (!ok) return null;

  return children;
}
