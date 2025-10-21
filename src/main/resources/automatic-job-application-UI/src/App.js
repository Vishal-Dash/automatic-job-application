import React, {useState} from 'react';


function App(){
const [q,setQ] = useState('');
const [results,setResults] = useState([]);
const [loading,setLoading] = useState(false);


async function doSearch(){
setLoading(true);
try{
const resp = await fetch('/api/search',{
method: 'POST',
headers: {'Content-Type':'application/json'},
body: JSON.stringify({query:q, k: