import { useState } from 'react';

export default function App() {
  const [input, setInput] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setResult(null);
    const paths = input.split('\n').map(s => s.trim()).filter(Boolean);
    if (paths.length === 0) {
      setError('Enter at least one path');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paths })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Request failed');
      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>Safe File Deleter</h1>
      <p className="hint">One relative path per line. Symlinks, non-regular files, and paths outside allowed_root are refused.</p>
      <form onSubmit={handleSubmit}>
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="docs/old.txt\ntmp/cache.bin"
          rows={8}
        />
        <button type="submit" disabled={loading}>{loading ? 'Deleting...' : 'Delete'}</button>
      </form>
      {error && <div className="error">{error}</div>}
      {result && (
        <div className="result">
          <div>
            <h3>Deleted ({result.deleted.length})</h3>
            <ul>{result.deleted.map(p => <li key={p}>{p}</li>)}</ul>
          </div>
          <div>
            <h3>Refused ({result.refused.length})</h3>
            <ul>{result.refused.map(p => <li key={p}>{p}</li>)}</ul>
          </div>
        </div>
      )}
    </div>
  );
}
