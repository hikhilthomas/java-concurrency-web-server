import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 1000,
  duration: '100s',
};

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