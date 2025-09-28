import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    vus: 250,
    duration: '20s',
};

const urls = [
    'http://localhost:4221/',
    'http://localhost:4221/io',
    'http://localhost:4221/compute',
];

export default function () {
    const url = urls[Math.floor(Math.random() * urls.length)];
    const res = http.get(url);
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}
