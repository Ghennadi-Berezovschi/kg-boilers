(function () {
    'use strict';
    const banner = document.getElementById('analytics-consent');
    if (!banner || window.location.pathname.startsWith('/admin')) return;
    const measurementId = banner.dataset.measurementId;
    if (!/^G-[A-Z0-9]{6,20}$/.test(measurementId || '')) return;

    const consentKey = 'pbs.analytics-consent.v1';
    const consentLifetime = 180 * 24 * 60 * 60 * 1000;
    const allowedServices = new Set(['boiler-installation', 'boiler-repair', 'boiler-service',
        'gas-safety-certificate', 'central-heating', 'hot-water-cylinder', 'plumbing',
        'plumbing-problems', 'bathroom-refurbishment', 'air-conditioning-installation',
        'gas-cooker-and-hob-installation', 'gas-pipework-and-gas-leak-detection']);
    let active = false;
    let started = false;

    function storedConsent() {
        try {
            const saved = JSON.parse(localStorage.getItem(consentKey));
            return saved && Date.now() - saved.at < consentLifetime &&
                ['accepted', 'rejected'].includes(saved.choice) ? saved.choice : null;
        } catch (_) { return null; }
    }

    // Only public routes and explicitly allowed campaign values leave the browser.
    // Never send form values, arbitrary query strings, page titles or URL fragments.
    function pageLocation() {
        const url = new URL(window.location.origin + window.location.pathname);
        const current = new URL(window.location.href);
        const service = current.searchParams.get('service');
        if (allowedServices.has(service)) url.searchParams.set('service', service);
        const campaignValues = {
            utm_source: new Set(['uk4ru', 'google', 'facebook', 'nextdoor', 'bing', 'yell', 'freeindex']),
            utm_medium: new Set(['referral', 'classified', 'cpc', 'organic', 'social']),
            utm_campaign: new Set(['boiler_services', 'boiler-services']),
            utm_content: new Set(['11328399'])
        };
        Object.keys(campaignValues).forEach(key => {
            const value = current.searchParams.get(key);
            if (campaignValues[key].has(value)) url.searchParams.set(key, value);
        });
        return url.href;
    }

    function referrer() {
        try {
            const url = new URL(document.referrer);
            return /^https?:$/.test(url.protocol) ? url.origin + url.pathname : '';
        } catch (_) { return ''; }
    }

    function track(name, parameters) {
        if (!active) return;
        window.gtag('event', name, Object.assign({
            page_location: pageLocation(), page_referrer: referrer(),
            page_title: 'Pro Boiler Services London', send_to: measurementId
        }, parameters || {}));
    }

    function start() {
        if (started) return;
        started = true;
        active = true;
        window['ga-disable-' + measurementId] = false;
        window.dataLayer = window.dataLayer || [];
        window.gtag = function () { window.dataLayer.push(arguments); };
        window.gtag('consent', 'default', { analytics_storage: 'denied', ad_storage: 'denied',
            ad_user_data: 'denied', ad_personalization: 'denied' });
        window.gtag('consent', 'update', { analytics_storage: 'granted', ad_storage: 'denied',
            ad_user_data: 'denied', ad_personalization: 'denied' });
        // Google may initialise the destination from its managed container before
        // processing our config command. Apply privacy defaults globally first.
        window.gtag('set', {
            send_page_view: false, allow_google_signals: false, allow_ad_personalization_signals: false,
            page_location: pageLocation(), page_referrer: referrer(), page_title: 'Pro Boiler Services London'
        });
        window.gtag('js', new Date());
        window.gtag('config', measurementId, {
            send_page_view: false, allow_google_signals: false, allow_ad_personalization_signals: false,
            page_location: pageLocation(), page_referrer: referrer(), page_title: 'Pro Boiler Services London'
        });
        const script = document.createElement('script');
        script.async = true;
        script.src = 'https://www.googletagmanager.com/gtag/js?id=' + measurementId;
        document.head.appendChild(script);
        track('page_view');
        if (banner.dataset.leadSubmitted === 'true') track('generate_lead', { lead_type: 'enquiry' });
    }

    function removeAnalyticsCookies() {
        document.cookie.split(';').forEach(item => {
            const name = item.trim().split('=')[0];
            if (!/^_ga(?:_|$)/.test(name)) return;
            const expiry = name + '=; Max-Age=0; path=/';
            document.cookie = expiry;
            document.cookie = expiry + '; domain=' + window.location.hostname;
            document.cookie = expiry + '; domain=.' + window.location.hostname;
        });
    }

    function choose(choice) {
        try { localStorage.setItem(consentKey, JSON.stringify({ choice: choice, at: Date.now() })); }
        catch (_) { /* A blocked preference store must not stop the quote journey. */ }
        banner.hidden = true;
        if (choice === 'accepted') start();
        else {
            active = false;
            window['ga-disable-' + measurementId] = true;
            removeAnalyticsCookies();
            if (started) window.location.replace(window.location.href);
        }
    }

    banner.querySelector('[data-analytics-accept]').addEventListener('click', () => choose('accepted'));
    banner.querySelector('[data-analytics-reject]').addEventListener('click', () => choose('rejected'));
    document.querySelectorAll('[data-analytics-settings]').forEach(button => {
        button.addEventListener('click', () => {
            banner.hidden = false;
            banner.querySelector('[data-analytics-reject]').focus();
        });
    });
    document.addEventListener('click', event => {
        const link = event.target.closest && event.target.closest('a[href]');
        if (!link) return;
        const href = link.getAttribute('href') || '';
        if (/^tel:/i.test(href)) track('phone_click', { contact_method: 'phone' });
        else {
            try {
                const url = new URL(href, window.location.origin);
                if (['wa.me', 'api.whatsapp.com'].includes(url.hostname))
                    track('whatsapp_click', { contact_method: 'whatsapp' });
            } catch (_) { /* Ignore invalid links. */ }
        }
    });
    const choice = storedConsent();
    if (choice === 'accepted') start();
    else if (choice !== 'rejected') banner.hidden = false;
})();
