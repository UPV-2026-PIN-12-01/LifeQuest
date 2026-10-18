import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import CharacterView from '../pages/CharacterView.jsx';
import HomePage from '../pages/HomePage.jsx';
import SignupPage from '../pages/SignupPage.jsx';
import { hasSession } from '../services/session.js';

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Redirige la raíz '/' automáticamente a '/character' */}
        <Route path="/" element={<Navigate to="/character" replace />} />
        
        {/* Tus rutas */}
        <Route path="/character" element={<CharacterView />} />
        <Route path="/login" element={<SignupPage />} />
        <Route path="/home" element={<RequireSession><HomePage /></RequireSession>} />
      </Routes>
    </BrowserRouter>
  );
}

function RequireSession({ children }) {
  return hasSession() ? children : <Navigate to="/login" replace />;
}
