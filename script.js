import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 1000,
  duration: '100s',
};

// Code for using mixed URLs 60%(Simple), 25%(IO), 15%(Compute)
//
// const endpoints = {
//     simple: 'http://localhost:4221/',
//     io: 'http://localhost:4221/io',
//     compute: 'http://localhost:4221/compute',
// };
//
// function pickEndpoint() {
//     const rnd = Math.random() * 100;
//     if (rnd < 60) return endpoints.simple;
//     if (rnd < 85) return endpoints.io;
//     return endpoints.compute;
// }

const urls = [
    'http://localhost:4221/',
    'http://localhost:4221/io',
    'http://localhost:4221/compute',
];

export default function () {
    // Pick one random endpoint
    let url = urls[Math.floor(Math.random() * urls.length)];
    let res = http.get(url);

    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}