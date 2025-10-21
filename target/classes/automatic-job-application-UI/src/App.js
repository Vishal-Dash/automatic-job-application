import React, { useState } from 'react';


function App() {
    const [q, setQ] = useState('');
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);


    async function doSearch() {
        setLoading(true);
        try {
            const resp = await fetch('/api/search', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query: q, k:import React, { useState } from 'react';


                function App(){
                const [q, setQ] = useState('');
                const [results, setResults] = useState([]);
                const [loading, setLoading] = useState(false);


                async function doSearch(){
                setLoading(true);
try{
                    const resp = await fetch('/api/search', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ query: q, k: 5 })
                    });
                    const data = await resp.json();
                    setResults(data);
                }catch(e) {
                    console.error(e);
                    alert('Search failed');
                }finally{ setLoading(false) }
            }
                                                    return (
                <div style={{ padding: 20, fontFamily: 'Arial' }}>
                    <h2>JobSearch Prototype</h2>
                    <div>
                        <input value={q} onChange={e => setQ(e.target.value)}
                            placeholder="e.g. Java backend" style={{ width: 400, padding: 8 }} />
                        <button onClick={doSearch} style={{ marginLeft: 8 }}>Search</button>
                    </div>
                    {loading && <p>Loading...</p>}
                    <ul>
                        {results.map((r, idx) => (
                            <li key={idx} style={{ marginTop: 12 }}>
                                <strong>{r.title}</strong> — {r.company}<br />
                                <small>score: {r.score.toFixed(3)}</small>
                                <p>{r.snippet}</p>
                            </li>
                        ))}
                    </ul>
                </div>
            );
        }
export default App;