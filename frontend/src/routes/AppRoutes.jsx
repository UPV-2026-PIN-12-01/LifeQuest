import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import CharacterView from '../pages/characterView';
import HelloPage from '../pages/helloPage';

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Redirige la raíz '/' automáticamente a '/character' */}
        <Route path="/" element={<Navigate to="/character" replace />} />
        
        {/* Tus rutas */}
        <Route path="/character" element={<CharacterView />} />
        <Route path="/hello" element={<HelloPage />} />
      </Routes>
    </BrowserRouter>
  );
}