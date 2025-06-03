function loadScreenings() {
    const startDate = new Date();
    startDate.setFullYear(startDate.getFullYear() - 1);
    const endDate = new Date();
    endDate.setFullYear(endDate.getFullYear() + 1);

    fetch(`${API_URL}/api/screenings/date-range?start=${startDate.toISOString()}&end=${endDate.toISOString()}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Không thể tải danh sách suất chiếu');
        }
        return response.json();
    })
    .then(screenings => {
        const screeningsList = document.getElementById('screenings-list');
        if (Array.isArray(screenings) && screenings.length > 0) {
            screenings.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
            screeningsList.innerHTML = screenings.map(screening => `
                <div class="bg-white rounded-xl shadow-md p-6 card-hover">
                    <div class="flex justify-between items-center">
                        <div>
                            <h3 class="text-xl font-semibold text-gray-900">${screening.movieTitle}</h3>
                            <p class="text-gray-600 mt-1">Thời gian: ${new Date(screening.startTime).toLocaleString()}</p>
                            <p class="text-gray-600 mt-1">Phòng: ${screening.hallNumber}</p>
                            <p class="text-gray-600 mt-1">Số ghế trống: ${screening.availableSeats}</p>
                            <p class="text-gray-600 mt-1">Giá vé: ${screening.price.toLocaleString('vi-VN')} VND</p>
                        </div>
                        <div class="flex space-x-3">
                            <button onclick="editScreening(${screening.id})"
                                    class="btn bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
                                Sửa
                            </button>
                            <button onclick="deleteScreening(${screening.id})"
                                    class="btn bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-600">
                                Xóa
                            </button>
                        </div>
                    </div>
                </div>
            `).join('');
        } else {
            screeningsList.innerHTML = `
                <div class="text-center text-gray-500">
                    <p>Chưa có suất chiếu nào.</p>
                </div>
            `;
        }
    })
    .catch(error => {
        console.error('Error:', error);
        document.getElementById('screenings-list').innerHTML = `
            <div class="text-center text-red-500">
                <p>Có lỗi xảy ra khi tải danh sách suất chiếu: ${error.message}</p>
                <button onclick="loadScreenings()" class="mt-3 btn bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700">
                    Thử lại
                </button>
            </div>
        `;
    });
}

function showAddScreeningForm() {
    document.getElementById('screening-form-title').textContent = 'Thêm Suất Chiếu';
    document.getElementById('screeningId').value = '';
    document.getElementById('screeningForm').reset();
    document.getElementById('screening-form').classList.remove('hidden');
    loadMoviesForSelect();
}

function hideScreeningForm() {
    document.getElementById('screening-form').classList.add('hidden');
    document.getElementById('screeningForm').reset();
}

function loadMoviesForSelect() {
    fetch(`${API_URL}/api/movies/public/all`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Không thể tải danh sách phim');
        }
        return response.json();
    })
    .then(movies => {
        if (!Array.isArray(movies)) {
            throw new Error('Dữ liệu phim không hợp lệ');
        }
        const movieSelect = document.getElementById('movieSelect');
        movieSelect.innerHTML = '<option value="">Chọn phim...</option>' +
            movies.map(movie => `<option value="${movie.id}">${movie.title}</option>`).join('');
    })
    .catch(error => {
        console.error('Error loading movies:', error);
        const movieSelect = document.getElementById('movieSelect');
        movieSelect.innerHTML = '<option value="">Lỗi tải danh sách phim</option>';
        alert('Có lỗi xảy ra khi tải danh sách phim: ' + error.message);
    });
}

function editScreening(screeningId) {
    fetch(`${API_URL}/api/screenings/${screeningId}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Không thể tải thông tin suất chiếu');
        }
        return response.json();
    })
    .then(screening => {
        document.getElementById('screening-form-title').textContent = 'Sửa Suất Chiếu';
        document.getElementById('screeningId').value = screening.id;
        loadMoviesForSelect();
        setTimeout(() => {
            document.getElementById('movieSelect').value = screening.movieId;
        }, 500);
        const startTime = new Date(screening.startTime);
        const formattedDateTime = startTime.toISOString().slice(0, 16);
        document.getElementById('startTime').value = formattedDateTime;
        document.getElementById('hallNumber').value = screening.hallNumber;
        document.getElementById('availableSeats').value = screening.availableSeats;
        document.getElementById('price').value = screening.price;
        document.getElementById('screening-form').classList.remove('hidden');
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Có lỗi xảy ra khi tải thông tin suất chiếu: ' + error.message);
    });
}

function deleteScreening(screeningId) {
    if (confirm('Bạn có chắc chắn muốn xóa suất chiếu này?')) {
        fetch(`${API_URL}/api/screenings/${screeningId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể xóa suất chiếu');
            }
            alert('Xóa suất chiếu thành công');
            loadScreenings();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi xảy ra khi xóa suất chiếu: ' + error.message);
        });
    }
}

document.getElementById('screeningForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const submitButton = this.querySelector('button[type="submit"]');
    submitButton.disabled = true;
    submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';

    try {
        if (!window.currentUser || !window.currentUser.roles || !window.currentUser.roles.includes('ROLE_ADMIN')) {
            alert('Bạn không có quyền thực hiện thao tác này!');
            return;
        }

        const screeningId = document.getElementById('screeningId').value;
        const screeningData = {
            movieId: parseInt(document.getElementById('movieSelect').value),
            startTime: new Date(document.getElementById('startTime').value).toISOString(),
            hallNumber: document.getElementById('hallNumber').value,
            availableSeats: parseInt(document.getElementById('availableSeats').value),
            price: parseInt(document.getElementById('price').value)
        };

        if (screeningId) {
            screeningData.id = parseInt(screeningId);
        }

        const response = await fetch(`${API_URL}/api/screenings`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(screeningData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Không thể ${screeningId ? 'cập nhật' : 'thêm'} suất chiếu`);
        }

        alert(`${screeningId ? 'Cập nhật' : 'Thêm'} suất chiếu thành công!`);
        hideScreeningForm();
        loadScreenings();
    } catch (error) {
        console.error('Error:', error);
        alert('Có lỗi xảy ra: ' + error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = 'Lưu';
    }
});