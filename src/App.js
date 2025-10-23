import React, { useState } from 'react';

function App() {
  const [q, setQ] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  async function doSearch() {
    setError(null);
    setLoading(true);
    try {
      const url = '/api/search?query=' + encodeURIComponent(q);
      const resp = await fetch(url, { method: 'GET' });
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
      const data = await resp.json();
      if (!Array.isArray(data)) throw new Error('Unexpected response from server');
      setResults(data);
    } catch (e) {
      console.error(e);
      setError(e.message || 'Search failed');
      setResults([]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ padding: 20, fontFamily: 'Arial' }}>
      <h2>JobSearch Prototype</h2>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <input
          value={q}
          onChange={e => setQ(e.target.value)}
          placeholder="e.g. Java backend"
          style={{ width: 440, padding: 8 }}
        />
        <button onClick={doSearch} style={{ padding: '8px 12px' }} disabled={loading || !q.trim()}>
          {loading ? 'Searching…' : 'Search'}
        </button>
      </div>

      {error && (
        <div style={{ marginTop: 12, color: 'crimson' }}>Error: {error}</div>
      )}

      <div style={{ marginTop: 16 }}>
        {loading && <p>Loading results…</p>}
        {!loading && results.length === 0 && !error && <p>No results yet. Try a different query.</p>}
        {!loading && results.length > 0 && (
          <div>
            <p>Showing {results.length} result{results.length > 1 ? 's' : ''}:</p>
            <ul style={{ paddingLeft: 18 }}>
              {results.map((r, idx) => (
                <li key={idx} style={{ marginTop: 12 }}>
                  <strong>{r.title ?? 'Untitled'}</strong> — {r.company ?? 'Unknown'}
                  <br />
                  <small>location: {r.location ?? 'N/A'} — similarity: {typeof r.similarity === 'number' ? r.similarity.toFixed(3) : 'n/a'}</small>
                  <p style={{ marginTop: 6 }}>
                    {r.link ? (
                      <a href={r.link} target="_blank" rel="noopener noreferrer">Apply / Details</a>
                    ) : null}
                  </p>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
