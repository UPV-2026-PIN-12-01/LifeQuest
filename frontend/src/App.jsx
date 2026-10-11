import { useEffect, useState } from 'react'
import HomePage from './pages/HomePage.jsx'
import SignupPage from './pages/SignupPage.jsx'

// Temporary hash switch until the app adds a router.
function currentPage() {
  return window.location.hash === '#/home' ? 'home' : 'signup'
}

function App() {
  const [page, setPage] = useState(currentPage)

  useEffect(() => {
    const onHashChange = () => setPage(currentPage())
    window.addEventListener('hashchange', onHashChange)
    return () => window.removeEventListener('hashchange', onHashChange)
  }, [])

  return page === 'home' ? <HomePage /> : <SignupPage />
}

export default App
