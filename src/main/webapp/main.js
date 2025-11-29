function categorizeBeans() {
    keys.forEach(type => categorized[type] = []);

    // Existing categories
    beans.forEach(bean => {
        categorized.Processed.push(bean.description || '');
    });

    categorized.SeenTypes = seenTypes.map(st => st.className + (st.annotations.length > 0 ? ' (' + st.annotations.join(', ') + ')' : ''));
    categorized.RestResources = Array.isArray(restResources) ? restResources.slice() : [];
    categorized.RestMethods = Array.isArray(restMethods) ? restMethods.slice() : [];
    categorized.RestExceptionMappers = Array.isArray(restExceptionMappers) ? restExceptionMappers.slice() : [];
    categorized.ScopedBeans = scopedBeans.slice();
    categorized.Producers = producers.slice();
    categorized.Interceptors = interceptors.slice();
    categorized.InterceptedClasses = interceptedClasses.slice();
    categorized.Decorators = decorators.slice();
    categorized.Extensions = extensions.slice();

    // Update tab counts
    Object.keys(categorized).forEach(type => {
        const countElem = document.getElementById('count-' + type);
        if (countElem) {
            countElem.textContent = categorized[type].length;
        }
    });

    // Handle SecurityAnnotations count
    const securityAnnotationsCountElem = document.getElementById('count-SecurityAnnotations');
    if (securityAnnotationsCountElem && securityAnnotations) {
        const secAnnCount = Object.keys(securityAnnotations).length;
        securityAnnotationsCountElem.textContent = secAnnCount;
    }

    // Observer events count
    const observerCountElem = document.getElementById('count-Observer');
    if (observerCountElem) {
        observerCountElem.textContent = observerEvents.length;
    }
    
    // Events recipient count
    const eventCountElem = document.getElementById('count-Events');
    if (eventCountElem) {
        eventCountElem.textContent = events.length;
    }
}


function parseSeenTypes(bean) {
    return {
        className: bean.className || bean,
        annotations: bean.annotations || []
    };
}

function parseProcessed(bean) {
    let type = bean.split("[")[0].trim();
    if (type.length > 25) {
        type = type.split(/[\s%]/)[0]; // split by space or %
    }
    if (type.includes("@")) {
        type = "Bean";
    }
    return {type: type, class: bean, details: ''};
}

// Parsing function for exception mappers
function parseRestExceptionMapper(bean) {
    debugger;
    if (typeof bean === 'string ') {
        return {
            mapperClass: bean,
            exceptionType: ''
        };
    } else if (bean && typeof bean === 'object') {
        const mapperClass = Object.keys(bean)[0];
        const exceptionType = bean[mapperClass];
        return {
            mapperClass: mapperClass,
            exceptionType: exceptionType || ''
        };
    }
    return {mapperClass: '', exceptionType: ''};
}

// Parsing function for the new tabs
function parseBeanDTO(bean) {
    return {
        type: bean.scope ? bean.scope.substring(bean.scope.lastIndexOf('.') + 1) : '',
        className: bean.className || ''
    };
}

function renderBeans() {
    const filter = document.getElementById('searchInput').value.toLowerCase();
    keys.forEach(type => {
        const tbody = document.getElementById(type + 'Table')?.querySelector('tbody');
        if (!tbody)
            return;

        tbody.innerHTML = '';
        let filteredBeans = [];

        if (type === 'RestExceptionMappers') {
            filteredBeans = categorized.RestExceptionMappers.filter(mapping => {
                const m = parseRestExceptionMapper(mapping);
                return m.mapperClass.toLowerCase().includes(filter) || (m.exceptionType && m.exceptionType.toLowerCase().includes(filter));
            });
        } else if (type === 'Processed') {
            filteredBeans = categorized[type].filter(bean => bean.toLowerCase().includes(filter));
        } else if (type === 'SeenTypes') {
            filteredBeans = categorized.SeenTypes.filter(bean => {
                return bean.toLowerCase().includes(filter);
            }).map(beanStr => {
                let namePart = beanStr;
                let annotationsPart = '';
                const match = beanStr.match(/^(.+?) \((.+)\)$/);
                if (match) {
                    namePart = match[1];
                    annotationsPart = match[2];
                }
                return {
                    className: namePart,
                    annotations: annotationsPart.split(', ').filter(a => a.length > 0)
                };
            });
        } else if (type === 'ScopedBeans') {
            categorized[type].forEach(bean => {
                const tr = document.createElement('tr');
                tr.onclick = (event) => openPane(event, 'BeanConsole', bean.className);
                const qualifiers = Array.isArray(bean.qualifiers) ? bean.qualifiers.join(', ') : '';
                const types = Array.isArray(bean.types) ? bean.types.join(', ') : '';
                tr.innerHTML = `
                                <td>${highlight(bean.scope ? bean.scope.substring(bean.scope.lastIndexOf('.') + 1) : '', filter)}</td>
                                <td>${highlight(bean.currentCount || '', filter)}</td>
                                <td>${highlight(bean.createdCount || '', filter)}</td>
                                <td>${highlight(bean.lastCreated || '', filter)}</td>
                                <td>${highlight(bean.maxCount || '', filter)}</td>
                                <td>${highlight(bean.destroyedCount || '', filter)}</td>
                                <td>${highlight(bean.className || '', filter)}</td>
                                <td>${highlight(types, filter)}</td>
                                <td>${highlight(bean.name || '', filter)}</td>
                                <td>${highlight(qualifiers, filter)}</td>
                            `;
                tbody.appendChild(tr);
            });
        } else if (type === 'InterceptedClasses') {
            filteredBeans = categorized[type].filter(bean => {
                const parsed = parseBeanDTO(bean);
                return parsed.type.toLowerCase().includes(filter) || parsed.className.toLowerCase().includes(filter);
            });
            filteredBeans.forEach(bean => {
                const p = parseBeanDTO(bean);
                const tr = document.createElement('tr');
                tr.onclick = (event) => openPane(event, 'BeanConsole', bean.className);
                const interceptorBindings = Array.isArray(bean.interceptorBindings) ? bean.interceptorBindings.join(', ') : '';
                tr.innerHTML = `
                                <td>${highlight(bean.className || '', filter)}</td>
                                <td>${highlight(interceptorBindings, filter)}</td>
                            `;
                tbody.appendChild(tr);
            });
        } else if (type === 'Producers') {
            filteredBeans = categorized[type].filter(p => JSON.stringify(p).toLowerCase().includes(filter));
            filteredBeans.forEach(bean => {
                const tr = document.createElement('tr');
                tr.onclick = (event) => openPane(event, 'BeanConsole', bean.className);
                tr.innerHTML = `
                                <td>${highlight(bean.className || '', filter)}</td>
                                <td>${highlight(bean.producedType || '', filter)}</td>
                                <td>${highlight(bean.kind || '', filter)}</td>
                                <td>${highlight(bean.memberSignature || '', filter)}</td>
                                <td>${highlight(bean.producedCount || '', filter)}</td>
                                <td>${highlight(bean.lastProduced || '', filter)}</td>
                            `;
                tbody.appendChild(tr);
            });
        } else if (type === 'Interceptors') {
            filteredBeans = categorized[type].filter(p => JSON.stringify(p).toLowerCase().includes(filter));
            filteredBeans.forEach(bean => {
                let scopeDisplay = bean.scope;
                if (scopeDisplay === 'jakarta.enterprise.context.Dependent') {
                    scopeDisplay = 'Dependent';
                }
                const tr = document.createElement('tr');
                tr.onclick = (event) => openPane(event, 'BeanConsole', bean.className);
                tr.innerHTML = `
                                <td>${highlight(bean.className || '', filter)}</td>
                                <td>${highlight((bean.interceptorBindings || []).join(', '), filter)}</td>
                                <td>${highlight(bean.priority != null ? bean.priority.toString() : '', filter)}</td>
                            `;
                tbody.appendChild(tr);
            });
        } else if (type === 'Decorators') {
            filteredBeans = categorized[type].filter(p => JSON.stringify(p).toLowerCase().includes(filter));
            filteredBeans.forEach(bean => {
                let scopeDisplay = bean.scope;
                if (scopeDisplay === 'jakarta.enterprise.context.Dependent') {
                    scopeDisplay = 'Dependent';
                }
                const tr = document.createElement('tr');
                tr.onclick = (event) => openPane(event, 'BeanConsole', bean.className);
                tr.innerHTML = `
                                <td>${highlight(bean.className || '', filter)}</td>
                                <td>${highlight(bean.delegateType || '', filter)}</td>
                            `;
                tbody.appendChild(tr);
            });
        } else if (type === 'Extensions') {
            filteredBeans = categorized[type].filter(name => name.toLowerCase().includes(filter));
            filteredBeans.forEach(name => {
                const tr = document.createElement('tr');
                tr.innerHTML = `<td>${highlight(name, filter)}</td>`;
                tbody.appendChild(tr);
            });
        } else if (type === 'Events') {
            filteredBeans = events.filter(event => {
                return event.eventType.toLowerCase().includes(filter) ||
                        event.firedBy.toLowerCase().includes(filter) ||
                        event.notifiedObservers.toString().toLowerCase().includes(filter);
            });
        } else {
            filteredBeans = observerEvents.filter(event => {
                return event.eventTypeName.toLowerCase().includes(filter) ||
                        event.className.toLowerCase().includes(filter) ||
                        event.reception.toLowerCase().includes(filter) ||
                        event.transactionPhase.toLowerCase().includes(filter);
            });
        }

        if (currentSortColumn[type] !== undefined) {
            const col = currentSortColumn[type];
            const order = currentSortOrder[type];
            filteredBeans.sort((a, b) => {
                let aVal, bVal;
                if (type === 'Observer') {
                    switch (col) {
                        case 0:
                            aVal = a.eventTypeName;
                            bVal = b.eventTypeName;
                            break;
                        case 1:
                            aVal = a.className;
                            bVal = b.className;
                            break;
                        case 2:
                            aVal = a.reception;
                            bVal = b.reception;
                            break;
                        case 3:
                            aVal = a.transactionPhase;
                            bVal = b.transactionPhase;
                            break;
                        default:
                            aVal = a.eventTypeName;
                            bVal = b.eventTypeName;
                    }
                } else if (type === 'Events') {
                    switch (col) {
                        case 0:
                            aVal = a.eventType;
                            bVal = b.eventType;
                            break;
                        case 1:
                            aVal = a.firedBy;
                            bVal = b.firedBy;
                            break;
                        case 2:
                            aVal = a.timestamp;
                            bVal = b.timestamp;
                            break;
                        case 3:
                            aVal = a.notifiedObservers.join(', ');
                            bVal = b.notifiedObservers.join(', ');
                            break;
                        default:
                            aVal = a.eventType;
                            bVal = b.eventType;
                    }
                } else if (type === 'SeenTypes') {
                    switch (col) {
                        case 0:
                            aVal = a.className.toLowerCase();
                            bVal = b.className.toLowerCase();
                            break;
                        case 1:
                            aVal = a.annotations.join(", ").toLowerCase();
                            bVal = b.annotations.join(", ").toLowerCase();
                            break;
                        default:
                            aVal = a.className.toLowerCase();
                            bVal = b.className.toLowerCase();
                    }
                } else if (type === 'RestExceptionMappers') {
                    const aParsed = parseRestExceptionMapper(a);
                    const bParsed = parseRestExceptionMapper(b);
                    aVal = col === 0 ? aParsed.mapperClass : aParsed.exceptionType;
                    bVal = col === 0 ? bParsed.mapperClass : bParsed.exceptionType;
                } else {
                    switch (type) {
                        case 'Processed':
                            aVal = Object.values(parseProcessed(a))[col];
                            bVal = Object.values(parseProcessed(b))[col];
                            break;
                        case 'RestResources':
                        case 'RestMethods':
                            aVal = (a.className || '').toLowerCase();
                            bVal = (b.className || '').toLowerCase();
                            break;
                        default:
                            aVal = a;
                            bVal = b;
                    }
                }
                aVal = aVal ? aVal.toLowerCase() : '';
                bVal = bVal ? bVal.toLowerCase() : '';
                if (aVal < bVal)
                    return order === 'asc' ? -1 : 1;
                if (aVal > bVal)
                    return order === 'asc' ? 1 : -1;
                return 0;
            });
        } else {
            if (type !== 'Observer') {
                filteredBeans.sort();
            }
        }

        if (!['ScopedBeans', 'InterceptedClasses', 'Producers', 'Interceptors', 'Decorators', 'Extensions', 'RestResources', 'RestMethods', 'Observer', 'Events', 'SeenTypes', 'Processed', 'RestExceptionMappers'].includes(type)) {
            filteredBeans.forEach(bean => {
                const tr = document.createElement('tr');
                tr.textContent = bean;
                tbody.appendChild(tr);
            });
        }

        // Render rows already specifically handled for some types
        if (type === 'Processed') {
            filteredBeans.forEach(bean => {
                const p = parseProcessed(bean);
                const tr = document.createElement('tr');
                tr.innerHTML = `
                              <td class='bean-type'>${p.type}</td>
                              <td class='bean-class'>${highlight(p.class, filter)}</td>
                              <td class='bean-details'>${highlight(p.details, filter)}</td>`;
                tbody.appendChild(tr);
            });
        } else if (type === 'Observer') {
            filteredBeans.forEach(bean => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                                <td>${highlight(bean.eventTypeName, filter)}</td>
                                <td>${highlight(bean.className, filter)}</td>
                                <td>${highlight(bean.reception, filter)}</td>
                                <td>${highlight(bean.transactionPhase, filter)}</td>`;
                tbody.appendChild(tr);
            });
        }  else if (type === 'Events') {
            filteredBeans.forEach(bean => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                                <td>${highlight(bean.eventType, filter)}</td>
                                <td>${highlight(bean.firedBy, filter)}</td>
                                <td>${highlight(bean.timestamp, filter)}</td>
                                <td>${highlight(bean.notifiedObservers, filter)}</td>`;
                tbody.appendChild(tr);
            });
        } else if (type === 'SeenTypes') {
            filteredBeans.forEach(bean => {
                const tr = document.createElement('tr');
                const p = bean;
                tr.innerHTML = `<td>${highlight(p.className, filter)}</td><td>${highlight(p.annotations.join(", "), filter)}</td>`;
                tbody.appendChild(tr);
            });
        } else if (type === 'RestExceptionMappers') {
            filteredBeans.forEach(bean => {
                const p = parseRestExceptionMapper(bean);
                const tr = document.createElement('tr');
                tr.innerHTML = `<td>${highlight(p.mapperClass, filter)}</td><td>${highlight(p.exceptionType, filter)}</td>`;
                tbody.appendChild(tr);
            });
        }
        restResources.forEach(bean => {
            if (type === 'RestResources') {
                const tr = document.createElement('tr');
                if (bean.className || bean.path) {
                    tr.innerHTML = `<td>${highlight(bean.className || '', filter)}</td><td>${highlight(bean.path || '', filter)}</td>`;
                } else {
                    tr.innerHTML = `<td>${highlight(bean, filter)}</td><td></td>`;
                }
                tbody.appendChild(tr);
            }
        });
    });
}

function filterBeans() {
    renderBeans();
    saveState();
}

function sortBeans(type) {
    currentSortColumn[type] = 0;
    currentSortOrder[type] = document.getElementById('sort-' + type)?.value || 'asc';
    renderBeans();
    saveState();
}

function sortColumn(type, colIndex) {
    if (currentSortColumn[type] === colIndex) {
        currentSortOrder[type] = currentSortOrder[type] === 'asc' ? 'desc' : 'asc';
    } else {
        currentSortColumn[type] = colIndex;
        currentSortOrder[type] = 'asc';
    }
    const sortSelect = document.getElementById('sort-' + type);
    if (sortSelect) {
        sortSelect.value = 'asc';
    }
    updateSortArrows(type);
    renderBeans();
    saveState();
}

function updateSortArrows(type) {
    const table = document.getElementById(type + 'Table');
    if (!table)
        return;
    const ths = table.querySelectorAll('th');
    ths.forEach((th, idx) => {
        th.classList.remove('sort-asc', 'sort-desc');
        if (idx === currentSortColumn[type]) {
            th.classList.add(currentSortOrder[type] === 'asc' ? 'sort-asc' : 'sort-desc');
            th.setAttribute('aria-sort', currentSortOrder[type] === 'asc' ? 'ascending' : 'descending');
        } else {
            th.setAttribute('aria-sort', 'none');
        }
    });
}


document.addEventListener('DOMContentLoaded', () => {
    fetchSecurityAnnotations();
    fetch('resources/dev/beans')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                beans = data;
                categorizeBeans();
                restoreState();
                filterBeans();
            })
            .catch(error => {
                console.error('Error fetching beans JSON:', error);
            });

    fetch('resources/dev/observers')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(eventsData => {
                observerEvents = eventsData;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'Observer') {
                    filterBeans();
                }
            })
            .catch(error => {
                console.error('Error fetching observer events:', error);
            });
            
    fetch('resources/dev/events')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(eventsData => {
                events = eventsData;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'Events') {
                    filterBeans();
                }
            })
            .catch(error => {
                console.error('Error fetching events:', error);
            });

    fetch('resources/dev/seen-types')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(seenTypesData => {
                seenTypes = seenTypesData;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'SeenTypes') {
                    filterBeans();
                }
            })
            .catch(error => {
                console.error('Error fetching seen types:', error);
            });

    fetch('resources/dev/rest-resources')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(restResourcesData => {
                restResources = restResourcesData;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'RestResources') {
                    filterBeans();
                }
            })
            .catch(error => {
                console.error('Error fetching REST resources:', error);
            });

    fetch('resources/dev/rest-methods')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(restMethodsData => {
                restMethods = restMethodsData;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'RestMethods') {
                    filterBeans();
                }
            })
            .catch(error => {
                console.error('Error fetching REST methods:', error);
            });

    fetch('resources/dev/rest-exception-mappers')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(exceptionMappersData => {
                restExceptionMappers = Object.entries(exceptionMappersData).map(([mapper, exType]) => ({[mapper]: exType}));
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'RestExceptionMappers') {
                    filterBeans();
                }
            })
            .catch(error => {
                console.error('Error fetching REST exception mappers:', error);
            });

    // Fetch new data for requested endpoints
    fetch('resources/dev/scoped-beans')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                scopedBeans = data;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'ScopedBeans') {
                    filterBeans();
                }
            })
            .catch(error => console.error('Error fetching scoped beans:', error));

    fetch('resources/dev/producers')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                producers = data;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'Producers') {
                    filterBeans();
                }
            })
            .catch(error => console.error('Error fetching producers:', error));

    fetch('resources/dev/interceptors')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                interceptors = data;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'Interceptors') {
                    filterBeans();
                }
            })
            .catch(error => console.error('Error fetching interceptors:', error));

    fetch('resources/dev/intercepted-classes')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                interceptedClasses = data;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'InterceptedClasses') {
                    filterBeans();
                }
            })
            .catch(error => console.error('Error fetching intercepted classes:', error));

    fetch('resources/dev/decorators')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                decorators = data;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'Decorators') {
                    filterBeans();
                }
            })
            .catch(error => console.error('Error fetching decorators:', error));

    fetch('resources/dev/extension')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                extensions = data;
                categorizeBeans();
                const activeTab = localStorage.getItem('activeTab') || 'Processed';
                if (activeTab === 'Extensions') {
                    filterBeans();
                }
            })
            .catch(error => console.error('Error fetching extensions:', error));

});