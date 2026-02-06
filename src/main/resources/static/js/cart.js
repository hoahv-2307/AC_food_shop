// Cart operations with AJAX

async function updateQuantity(itemId, newQuantity) {
    if (newQuantity < 1) {
        if (!confirm('Remove this item from cart?')) {
            return;
        }
        return removeItem(itemId);
    }

    try {
        const response = await fetch(`/cart/items/${itemId}?quantity=${newQuantity}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        });

        const result = await response.json();

        if (result.success) {
            // Reload page to update cart
            window.location.reload();
        } else {
            alert('Failed to update cart. Please try again.');
        }
    } catch (error) {
        console.error('Error updating cart:', error);
        alert('Failed to update cart. Please try again.');
    }
}

async function removeItem(itemId) {
    try {
        const response = await fetch(`/cart/items/${itemId}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (result.success) {
            // Update cart count in navbar
            const cartBadge = document.querySelector('.navbar .badge');
            if (cartBadge) {
                cartBadge.textContent = result.cartItemCount;
            }

            // Reload page to update cart
            window.location.reload();
        } else {
            alert('Failed to remove item. Please try again.');
        }
    } catch (error) {
        console.error('Error removing item:', error);
        alert('Failed to remove item. Please try again.');
    }
}

// Show loading state on checkout
document.querySelector('form[action*="/orders/create"]')?.addEventListener('submit', function(e) {
    const submitBtn = this.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Processing...';
});
