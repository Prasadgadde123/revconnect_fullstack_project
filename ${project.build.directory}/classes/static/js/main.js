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
});
