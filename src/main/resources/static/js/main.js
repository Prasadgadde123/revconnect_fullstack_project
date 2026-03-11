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
            e.preventDefault();
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
    // Let clicks on links/buttons inside the dropdown actually close the dropdown while preserving their default action
    document.querySelectorAll('.post-dropdown').forEach(d => {
        d.addEventListener('click', e => {
            // we don't stop propagation here so that the document click listener can close the menu
            // BUT we let the default action happen (form submit, 'onclick' for modal, etc.)
        });
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
        const toggleBtn = profileMenu.querySelector('.dock-item-v2');
        const dropdown = profileMenu.querySelector('.dropdown-menu');

        if (toggleBtn && dropdown) {
            toggleBtn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                const isOpen = dropdown.style.display === 'block';
                dropdown.style.display = isOpen ? 'none' : 'block';
            });

            // Close when clicking outside
            document.addEventListener('click', (e) => {
                if (!profileMenu.contains(e.target)) {
                    dropdown.style.display = 'none';
                }
            });
        }

        profileMenu.addEventListener('touchstart', function (e) {
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
    // Removal of btn-interact legacy handlers as they are replaced by universal follow/connect

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
    // UNIVERSAL CONNECT BUTTON (AJAX)
    // ============================================================
    document.querySelectorAll('.connect-btn-ajax').forEach(btn => {
        btn.addEventListener('click', async function (e) {
            e.preventDefault();
            if (this.classList.contains('loading') || this.disabled) return;

            const userId = this.dataset.userId;
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

            const originalText = this.textContent;
            this.classList.add('loading');
            this.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>`;

            try {
                const res = await fetch(`/api/connections/request/${userId}`, {
                    method: 'POST',
                    headers: { 'X-CSRF-TOKEN': csrfToken }
                });

                if (res.ok) {
                    const data = await res.json();
                    if (data.success) {
                        this.textContent = 'Pending';
                        this.disabled = true;
                        this.classList.remove('btn-primary', 'premium-btn-shimmer', 'sophie-follow-btn', 'btn-luxury-primary');
                        this.classList.add('btn-outline-secondary', 'btn-luxury-outline');
                    } else {
                        console.error('Connection request failed:', data.message);
                        this.textContent = originalText;
                    }
                } else {
                    this.textContent = originalText;
                }
            } catch (err) {
                console.error('Connection request failed:', err);
                this.textContent = originalText;
            } finally {
                this.classList.remove('loading');
            }
        });
    });

    // ============================================================
    // UNIVERSAL FOLLOW BUTTON (AJAX)
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
                const isFollowing = this.textContent.trim().toUpperCase() === 'FOLLOWING';
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
                        this.textContent = this.dataset.activeText || (this.classList.contains('btn-luxury') ? 'FOLLOWING' : 'Following');
                        this.classList.remove('btn-primary', 'btn-luxury-primary');
                        this.classList.add('btn-outline-primary', 'btn-luxury-outline');
                    } else {
                        this.textContent = this.dataset.inactiveText || (this.classList.contains('btn-luxury') ? 'FOLLOW' : 'Follow');
                        this.classList.remove('btn-outline-primary', 'btn-luxury-outline');
                        this.classList.add('btn-primary', 'btn-luxury-primary');
                    }

                    // Update counts in the UI
                    // 1. Update the follower/following count on the current profile card (if on profile page)
                    const profileFollowerCount = document.querySelector('.profile-header-card .fw-bold.fs-5, .premium-hero-header-v2 .premium-stat-val, .sophie-hero-container .sophie-stat-item span, .luxury-stat-item[data-stat="followers"] .luxury-stat-value');
                    const profileFollowingCount = document.querySelector('.luxury-stat-item[data-stat="following"] .luxury-stat-value');

                    if (profileFollowerCount && data.followerCount !== undefined) {
                        profileFollowerCount.textContent = data.followerCount;
                    }
                    if (profileFollowingCount && data.followingCount !== undefined) {
                        profileFollowingCount.textContent = data.followingCount;
                    }

                    // 2. Update stats on mini-cards or suggested accounts sidebar
                    const cardContainer = this.closest('.post-card, .profile-card, .suggested-user-card, .luxury-profile-card');
                    if (cardContainer) {
                        const cardFollowerCount = cardContainer.querySelector('.follower-count-text, .luxury-stat-value');
                        if (cardFollowerCount && data.followerCount !== undefined) {
                            cardFollowerCount.textContent = data.followerCount;
                        }
                    }
                } else {
                    this.textContent = originalText;
                }
            } catch (err) {
                console.error('Follow failed:', err);
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
    // ============================================================
    // PASSWORD STRENGTH INDICATOR
    // ============================================================
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    passwordInputs.forEach(input => {
        if (!input.id.includes('Confirm') && !input.name.includes('confirm')) {
            const meter = document.createElement('div');
            meter.className = 'password-strength-meter';
            meter.innerHTML = '<div class="password-strength-fill"></div>';
            input.parentElement.after(meter);

            input.addEventListener('input', () => {
                const val = input.value;
                const fill = meter.querySelector('.password-strength-fill');
                meter.style.display = val.length > 0 ? 'block' : 'none';

                if (val.length < 5) {
                    fill.className = 'password-strength-fill weak';
                } else if (val.length < 10) {
                    fill.className = 'password-strength-fill medium';
                } else {
                    fill.className = 'password-strength-fill strong';
                }
            });
        }
    });
    // ============================================================
    // INFINITE SCROLL (Intersection Observer)
    // ============================================================
    const sentinel = document.getElementById('feed-sentinel');
    const feedContainer = document.getElementById('feed-container');
    const loader = document.getElementById('feed-loader');
    const paginationData = document.getElementById('pagination-data');

    if (sentinel && feedContainer && paginationData) {
        let currentPage = parseInt(paginationData.dataset.currentPage);
        const totalPages = parseInt(paginationData.dataset.totalPages);
        let loading = false;

        const loadMore = async () => {
            if (loading || currentPage + 1 >= totalPages) return;
            loading = true;
            if (loader) loader.style.display = 'block';

            try {
                const nextPage = currentPage + 1;
                const params = new URLSearchParams(window.location.search);
                params.set('page', nextPage);

                const res = await fetch(`/feed/fragment?${params.toString()}`);
                if (res.ok) {
                    const html = await res.text();
                    const temp = document.createElement('div');
                    temp.innerHTML = html;

                    const newPosts = temp.querySelectorAll('.post-card');
                    newPosts.forEach(post => {
                        post.style.opacity = '0';
                        feedContainer.appendChild(post);
                        requestAnimationFrame(() => {
                            post.style.transition = 'opacity 0.5s ease-in-out';
                            post.style.opacity = '1';
                        });
                    });

                    currentPage = nextPage;
                    paginationData.dataset.currentPage = currentPage;

                    if (currentPage + 1 >= totalPages) {
                        observer.unobserve(sentinel);
                        sentinel.innerHTML = '<p class="text-center text-muted small mt-4">You\'ve caught up with everything! ✨</p>';
                    }
                }
            } catch (err) {
                console.error('Failed to load more posts:', err);
            } finally {
                loading = false;
                if (loader) loader.style.display = 'none';
            }
        };

        const observer = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting) {
                loadMore();
            }
        }, { rootMargin: '200px' });

        observer.observe(sentinel);
    }
});
