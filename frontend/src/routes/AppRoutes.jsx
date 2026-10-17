import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import CharacterView from '../pages/CharacterView';
import HelloPage from '../pages/HelloPage';
import SignupPage from '../pages/SignupPage.jsx';

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Redirige la raíz '/' automáticamente a '/character' */}
        <Route path="/" element={<Navigate to="/character" replace />} />
        
        {/* Tus rutas */}
        <Route path="/character" element={<CharacterView />} />
        <Route path="/hello" element={<HelloPage />} />
        <Route path="/signup" element={<SignupPage />} />
      </Routes>
    </BrowserRouter>
  );
}