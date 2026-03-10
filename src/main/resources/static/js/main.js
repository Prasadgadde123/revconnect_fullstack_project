// RevConnect - Main JavaScript

document.addEventListener('DOMContentLoaded', function () {

    // ============================================================
    // LIKE BUTTON (AJAX)
    // ============================================================
    document.querySelectorAll('.btn-like').forEach(btn => {
        btn.addEventListener('click', async function (e) {
            e.preventDefault();
            const postId = this.dataset.postId;
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content
                || document.querySelector('input[name="_csrf"]')?.value;

            try {
                const res = await fetch(`/post/${postId}/like`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrfToken
                    }
                });
                if (res.ok) {
                    const data = await res.json();
                    const icon = this.querySelector('.action-icon');
                    const count = this.querySelector('.like-count');

                    if (icon) {
                        // Color: Red #ef4444 for liked, none/currentColor for unliked
                        icon.setAttribute('fill', data.liked ? '#ef4444' : 'none');
                        icon.setAttribute('stroke', data.liked ? '#ef4444' : 'currentColor');
                    }

                    if (count) count.textContent = data.count;
                    this.classList.toggle('liked', data.liked);
                }
            } catch (err) {
                console.error('Like failed:', err);
            }
        });
    });

    // ============================================================
    // POST ACTIONS DROPDOWN (⋯ button) — click toggle
    // ============================================================
    document.querySelectorAll('.btn-menu').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            const dropdown = this.nextElementSibling;
            const isOpen = dropdown.style.display === 'block';
            // Close all open dropdowns first
            document.querySelectorAll('.post-dropdown').forEach(d => d.style.display = 'none');
            dropdown.style.display = isOpen ? 'none' : 'block';
        });
    });
    // Close dropdowns when clicking anywhere outside
    document.addEventListener('click', () => {
        document.querySelectorAll('.post-dropdown').forEach(d => d.style.display = 'none');
    });
    document.querySelectorAll('.post-dropdown').forEach(d => {
        d.addEventListener('click', e => e.stopPropagation());
    });

    // ============================================================
    // DARK MODE TOGGLE (Glassmorphic Switch)
    // ============================================================
    const darkToggle = document.getElementById('darkModeToggle');
    const applyTheme = (dark) => {
        document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    };
    // Apply saved preference on load
    const savedTheme = localStorage.getItem('theme') === 'dark';
    applyTheme(savedTheme);
    if (darkToggle) {
        darkToggle.addEventListener('click', () => {
            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            localStorage.setItem('theme', isDark ? 'light' : 'dark');
            applyTheme(!isDark);
        });
    }

    // Get CSRF for AJAX - inject as meta tag if needed
    const csrfInput = document.querySelector('input[name="_csrf"]');
    if (csrfInput) {
        let meta = document.querySelector('meta[name="_csrf"]');
        if (!meta) {
            meta = document.createElement('meta');
            meta.name = '_csrf';
            document.head.appendChild(meta);
        }
        meta.content = csrfInput.value;
    }

    // ============================================================
    // CHARACTER COUNTER FOR POST TEXTAREA
    // ============================================================
    const postTextarea = document.querySelector('.post-textarea');
    const charCount = document.getElementById('charCount');
    if (postTextarea && charCount) {
        const updateCount = () => {
            const len = postTextarea.value.length;
            charCount.textContent = len;
            charCount.style.color = len > 1800 ? '#ef4444' : '';
        };
        postTextarea.addEventListener('input', updateCount);
        updateCount();
    }

    // ============================================================
    // AUTO-DISMISS ALERTS
    // ============================================================
    document.querySelectorAll('.alert').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 4000);
    });

    // ============================================================
    // CONFIRM BEFORE DELETE
    // ============================================================
    document.querySelectorAll('form[onsubmit]').forEach(form => {
        // Already handled inline via onsubmit attribute
    });

    // ============================================================
    // MOBILE NAV DROPDOWN
    // ============================================================
    // Dropdown is handled via CSS :hover but let's add touch support
    const profileMenu = document.querySelector('.nav-profile-menu');
    if (profileMenu) {
        profileMenu.addEventListener('touchstart', function (e) {
            const dropdown = this.querySelector('.dropdown-menu');
            if (dropdown) {
                dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
                e.preventDefault();
            }
        });
    }

    // ============================================================
    // HASHTAG INPUT FORMATTER
    // ============================================================
    const hashtagInput = document.querySelector('input[name="hashtags"]');
    if (hashtagInput) {
        hashtagInput.addEventListener('blur', function () {
            let val = this.value.trim();
            // Remove # signs for cleaner display
            val = val.split(/[,\s]+/)
                .map(t => t.replace(/^#/, '').trim())
                .filter(Boolean)
                .join(', ');
            this.value = val;
        });
    }

    // ============================================================
    // SMOOTH PAGE TRANSITIONS
    // ============================================================
    // Simplified load
    requestAnimationFrame(() => {
        document.body.style.opacity = '1';
    });

    // ============================================================
    // NOTIFICATION BADGE POLLING (every 30s)
    // ============================================================
    // Simple refresh approach - no WebSocket needed for basic implementation
    // Can be enhanced with SSE or WebSockets later

    // ============================================================
    // CYBER CARD INTERACTIONS (Loading States)
    // ============================================================
    document.querySelectorAll('.btn-interact').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            if (this.classList.contains('loading')) return;

            const action = this.dataset.action;
            const originalHtml = this.innerHTML;

            this.classList.add('loading');

            // Simulate API Call
            setTimeout(() => {
                this.classList.remove('loading');
                if (action === 'connect') {
                    this.innerHTML = `
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        Connected
                    `;
                    this.classList.remove('cyber-btn-primary');
                    this.classList.add('cyber-btn-outline');
                    this.style.borderColor = 'var(--neon-green)';
                    this.style.color = 'var(--neon-green)';
                } else if (action === 'message') {
                    // Just a subtle feedback for message
                    this.style.borderColor = 'var(--neon-blue)';
                    this.innerHTML = `
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                        Sent
                    `;
                }
            }, 1200);
        });
    });

    // ============================================================
    // PROFILE TABS (Vanilla Bootstrap-like behavior)
    // ============================================================
    const tabButtons = document.querySelectorAll('#profileTabs button[data-bs-toggle="tab"]');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const targetId = this.getAttribute('data-bs-target');
            const targetPanel = document.querySelector(targetId);

            if (targetPanel) {
                // Deactivate all
                document.querySelectorAll('#profileTabs .nav-link').forEach(t => t.classList.remove('active'));
                document.querySelectorAll('.tab-pane').forEach(p => {
                    p.classList.remove('show', 'active');
                });

                // Activate clicked
                this.classList.add('active');
                targetPanel.classList.add('show', 'active');
            }
        });
    });

    // ============================================================
    // UNIVERSAL FOLLOW/CONNECT BUTTON (AJAX)
    // ============================================================
    document.querySelectorAll('.follow-btn-ajax').forEach(btn => {
        btn.addEventListener('click', async function (e) {
            e.preventDefault();
            if (this.classList.contains('loading')) return;

            const userId = this.dataset.userId;
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

            const originalText = this.textContent;
            this.classList.add('loading');
            this.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>`;

            try {
                // Determine action based on current state
                const isFollowing = this.classList.contains('btn-outline-primary') || this.textContent.trim().toUpperCase() === 'CONNECTED' || this.textContent.trim().toUpperCase() === 'FOLLOWING';
                const action = isFollowing ? 'unfollow' : 'follow';

                const url = action === 'follow' ? `/follow/${userId}` : `/follow/unfollow/${userId}`;

                const res = await fetch(url, {
                    method: 'POST',
                    headers: { 'X-CSRF-TOKEN': csrfToken }
                });

                if (res.ok) {
                    const data = await res.json();
                    // Toggle appearance
                    if (data.isFollowing) {
                        this.textContent = this.classList.contains('btn-luxury') ? 'CONNECTED' : 'Following';
                        this.classList.remove('btn-primary');
                        this.classList.add('btn-outline-primary');
                    } else {
                        this.textContent = this.classList.contains('btn-luxury') ? '+ ADD' : 'Follow';
                        this.classList.remove('btn-outline-primary');
                        this.classList.add('btn-primary');
                    }

                    // Update counts in the UI if we find the stat containers
                    const followerStat = document.querySelectorAll('.luxury-stat-item').item(0)?.querySelector('.luxury-stat-value');
                    const followingStat = document.querySelectorAll('.luxury-stat-item').item(1)?.querySelector('.luxury-stat-value');

                    if (followerStat) followerStat.textContent = data.followerCount;
                    if (followingStat) followingStat.textContent = data.followingCount;

                    // Also update premium stat pills if they exist
                    document.querySelectorAll('.premium-stat-pill').forEach(pill => {
                        const label = pill.querySelector('.premium-stat-label')?.textContent.toLowerCase();
                        const val = pill.querySelector('.premium-stat-val');
                        if (label === 'followers') val.textContent = data.followerCount;
                        if (label === 'following') val.textContent = data.followingCount;
                    });
                } else {
                    this.textContent = originalText;
                }
            } catch (err) {
                console.error('Operation failed:', err);
                this.textContent = originalText;
            } finally {
                this.classList.remove('loading');
            }
        });
    });

    // ============================================================
    // PREMIUM 3D TILT EFFECT (For Hero Cards)
    // ============================================================
    const premiumCards = document.querySelectorAll('.premium-hero-header');

    premiumCards.forEach(area => {
        const card = area.querySelector('.card');
        if (!card) return;

        area.addEventListener('mousemove', (e) => {
            const rect = area.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const centerX = rect.width / 2;
            const centerY = rect.height / 2;

            // Subtle rotation (max 10 degrees)
            const rotateX = (centerY - y) / 25;
            const rotateY = (x - centerX) / 25;

            card.style.transform = `rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
        });

        area.addEventListener('mouseleave', () => {
            card.style.transform = `rotateX(0deg) rotateY(0deg)`;
        });
    });
});
