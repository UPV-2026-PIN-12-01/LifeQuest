import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AuthenticatedLayout from '../components/AuthenticatedLayout.jsx';
import CharacterView from '../pages/CharacterView.jsx';
import ChallengesPage from '../pages/ChallengesPage.jsx';
import HomePage from '../pages/HomePage.jsx';
import ProfilePage from '../pages/ProfilePage.jsx';
import SignupPage from '../pages/SignupPage.jsx';
import { hasSession } from '../services/session.js';

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to={hasSession() ? '/home' : '/login'} replace />} />
        
        <Route path="/login" element={<SignupPage />} />
        <Route element={<RequireSession><AuthenticatedLayout /></RequireSession>}>
          <Route path="/character" element={<CharacterView />} />
          <Route path="/challenges" element={<ChallengesPage />} />
          <Route path="/home" element={<HomePage />} />
          <Route path="/settings" element={<ProfilePage />} />
        </Route>
        <Route path="/profile" element={<Navigate to="/settings" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

function RequireSession({ children }) {
  return hasSession() ? children : <Navigate to="/login" replace />;
}
