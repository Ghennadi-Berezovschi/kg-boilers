import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

const code = readFileSync(new URL('../src/main/resources/static/js/analytics.js', import.meta.url), 'utf8');
function browser({ choice, submitted = false, path = '/', query = '', storageBlocked = false } = {}) {
    const listeners = {};
    const scripts = [];
    const storage = new Map();
    const cookieWrites = [];
    if (choice) storage.set('pbs.analytics-consent.v1', JSON.stringify({ choice, at: Date.now() }));
    const banner = { hidden: true, dataset: { measurementId: 'G-TEST123456', leadSubmitted: String(submitted) },
        querySelector: selector => ({ addEventListener: (_, fn) => listeners[selector] = fn, focus() {} }) };
    const document = {
        referrer: 'https://uk4ru.com/view/ad?email=private@example.com#private',
        head: { appendChild: script => scripts.push(script) },
        getElementById: () => banner,
        createElement: () => ({}),
        querySelectorAll: () => [{ addEventListener: (_, fn) => listeners.settings = fn }],
        addEventListener: (_, fn) => listeners.contact = fn,
        get cookie() { return '_ga=visitor; _ga_TEST123456=session; JSESSIONID=essential'; },
        set cookie(value) { cookieWrites.push(value); }
    };
    const window = { location: { origin: 'https://proboilerserviceslondon.co.uk', hostname: 'proboilerserviceslondon.co.uk',
        pathname: path, href: 'https://proboilerserviceslondon.co.uk' + path + query,
        replace(value) { listeners.reloaded = value; } } };
    const localStorage = { getItem: key => { if (storageBlocked) throw Error(); return storage.get(key); },
        setItem: (key, value) => { if (storageBlocked) throw Error(); storage.set(key, value); } };
    vm.runInNewContext(code, { window, document, localStorage, URL, Date, Set, JSON });
    return { window, banner, scripts, listeners, cookieWrites,
        accept: () => listeners['[data-analytics-accept]'](),
        reject: () => listeners['[data-analytics-reject]'](),
        events: () => Array.from(window.dataLayer || []).filter(args => args[0] === 'event') };
}

test('no Google tag or events before consent or after rejection', () => {
    for (const choice of [undefined, 'rejected']) {
        const b = browser({ choice, submitted: true });
        assert.equal(b.scripts.length, 0);
        assert.equal(b.events().length, 0);
    }
    const b = browser();
    b.reject();
    b.listeners.contact({ target: { closest: () => ({ getAttribute: () => 'tel:+447552532075' }) } });
    assert.equal(b.scripts.length, 0);
    assert.equal(b.events().length, 0);
});

test('consent loads asynchronously once and strips personal query and referrer data', () => {
    const b = browser({ query: '?utm_source=uk4ru&utm_medium=referral&utm_campaign=boiler_services&utm_content=11328399&email=private@example.com&postcode=E154DH#name' });
    b.accept(); b.accept();
    assert.equal(b.scripts.length, 1);
    assert.equal(b.scripts[0].async, true);
    assert.equal(b.events().length, 1);
    const event = b.events()[0];
    assert.equal(event[1], 'page_view');
    const commands = Array.from(b.window.dataLayer);
    const defaults = commands.findIndex(args => args[0] === 'set');
    assert.ok(defaults < commands.findIndex(args => args[0] === 'js'));
    assert.equal(commands[defaults][1].send_page_view, false);
    assert.equal(commands[defaults][1].page_title, 'Pro Boiler Services London');
    assert.match(event[2].page_location, /utm_source=uk4ru/);
    assert.doesNotMatch(JSON.stringify(b.window.dataLayer), /private@example|E154DH|#name/);
    assert.equal(event[2].page_referrer, 'https://uk4ru.com/view/ad');
});

test('tracks contact intent without phone, link URL or form values; lead requires success', () => {
    const b = browser({ choice: 'accepted', submitted: true });
    for (const href of ['tel:+447552532075', 'https://wa.me/447552532075?text=private']) {
        b.listeners.contact({ target: { closest: () => ({ getAttribute: () => href }) } });
    }
    assert.deepEqual(b.events().map(e => e[1]), ['page_view', 'generate_lead', 'phone_click', 'whatsapp_click']);
    assert.equal(b.events()[2][2].contact_method, 'phone');
    assert.equal(b.events()[3][2].contact_method, 'whatsapp');
    assert.doesNotMatch(JSON.stringify(b.events()), /447552|private/);
    assert.equal(browser({ choice: 'accepted' }).events().filter(e => e[1] === 'generate_lead').length, 0);
});

test('withdrawal disables collection, removes only analytics cookies and reloads as GET', () => {
    const b = browser({ choice: 'accepted' });
    b.reject();
    assert.equal(b.window['ga-disable-G-TEST123456'], true);
    assert.equal(b.cookieWrites.length, 6);
    assert.ok(b.cookieWrites.every(c => !c.includes('JSESSIONID')));
    assert.equal(b.listeners.reloaded, b.window.location.href);
});

test('blocked preference storage and admin pages never break the site', () => {
    const b = browser({ storageBlocked: true });
    assert.equal(b.banner.hidden, false);
    assert.doesNotThrow(() => b.reject());
    assert.equal(browser({ choice: 'accepted', path: '/admin/quotes' }).scripts.length, 0);
});
