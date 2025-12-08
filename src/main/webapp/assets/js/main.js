document.addEventListener('DOMContentLoaded', () => {
    loadSidePaneCounts();
    debugger;
    const last = localStorage.getItem("activeTab") || "CDIDevConsole";
    const btn = document.getElementById("side-" + last);

    if (btn) {
        btn.click();      // triggers openPane()
    }
});

async function loadSidePaneCounts() {
    try {
        const res = await fetch("resources/dev/metadata");
      
        if (!res.ok) return;
        const meta = await res.json();

        // Mapping metadata fields → span IDs
        const map = {
            scopedBeanCount: "count-ScopedBeans",
            producerCount: "count-Producers",
            interceptorCount: "count-Interceptors",
            interceptedClassesCount: "count-InterceptedClasses",
            decoratorCount: "count-Decorators",
            decoratedClassesCount: "count-DecoratedClasses",
            extensionCount: "count-Extensions",
            observerCount: "count-Observers",
            recentEventCount: "count-Events",
            beanCount: "count-Processed",
            seenTypeCount: "count-SeenTypes",
            restResourceCount: "count-RestResources",
            restMethodCount: "count-RestMethods",
            restExceptionMapperCount: "count-RestExceptionMappers",
            securityAnnotationCount: "count-SecurityAnnotations",
        };

        // Add missing derived values
        meta.seenTypeCount = meta.seenTypeCount ?? 0; // if needed

        Object.entries(map).forEach(([metaKey, spanId]) => {
            const el = document.getElementById(spanId);
            if (el && meta[metaKey] != null) {
                el.textContent = meta[metaKey];
                el.style.display = "inline-block";
            }
        });

    } catch (e) {
        console.error("Failed to load sidebar counts", e);
    }
}
