function openPane(evt, tabName, id) {
    const iframe = document.getElementById('dashboardIframe');
    const contentArea = document.getElementById('contentArea');
 const iframeSources = {
        CDIDevConsole: "cdi-dashboard.html",
        MetricsDashboard: "metrics-dashboard.html",
        BeanConsole: "bean-dashboard.html",
        RestMethods: "rest-endpoints-dashboard.html"
    };
    // Show the dashboard iframe only when requested, otherwise hide it
    if (tabName === 'CDIDevConsole' || tabName === 'MetricsDashboard' 
            || tabName === 'BeanConsole' || tabName === 'RestMethods') {
        iframe.style.display = 'block';
        contentArea.style.display ='none';
         if (iframe.src !== iframeSources[tabName]) {
            if (tabName === 'BeanConsole') {
                iframe.src = iframeSources[tabName] + "?bean=" + encodeURIComponent(id);
            } else if (tabName === 'RestConsole') {
                iframe.src = iframeSources[tabName];
            } else {
                iframe.src = iframeSources[tabName];
            }
        }
    } else {
        iframe.style.display = 'none';
        contentArea.style.display ='block';
    }

    const tabcontent = document.getElementsByClassName('tabcontent');
    for (const tab of tabcontent) {
        tab.style.display = 'none';
        tab.setAttribute('aria-hidden', 'true');
    }

    const sideButtons = document.getElementsByClassName('sideButton');
    for (const btn of sideButtons) {
        btn.classList.remove('active');
        btn.setAttribute('aria-selected', 'false');
    }

    const activeTab = document.getElementById(tabName);
    if (activeTab) {
        activeTab.style.display = 'block';
        activeTab.setAttribute('aria-hidden', 'false');
    }

    if (evt && evt.currentTarget) {
        evt.currentTarget.classList.add('active');
        evt.currentTarget.setAttribute('aria-selected', 'true');
        evt.currentTarget.focus();
    }

    localStorage.setItem('activeTab', tabName);

    filterBeans();
}

document.getElementById('sidePane').addEventListener('keydown', e => {
    const btns = Array.from(document.querySelectorAll('.sideButton'));
    let idx = btns.findIndex(b => b.getAttribute('aria-selected') === 'true');
    if (e.key === 'ArrowDown') {
        idx = (idx + 1) % btns.length;
        btns[idx].focus();
        btns[idx].click();
        e.preventDefault();
    } else if (e.key === 'ArrowUp') {
        idx = (idx - 1 + btns.length) % btns.length;
        btns[idx].focus();
        btns[idx].click();
        e.preventDefault();
    }
});