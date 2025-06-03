function loadMovies() {
    const moviesList = document.getElementById('movies-list');
    moviesList.innerHTML = `
        <div class="col-span-full text-center">
            <div class="spinner"></div>
            <p class="mt-3 text-gray-600">Đang tải danh sách phim...</p>
        </div>
    `;

    return fetch(`${API_URL}/api/movies/public/all`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải danh sách phim');
            }
            return response.json();
        })
        .then(movies => {
            if (Array.isArray(movies) && movies.length > 0) {
                moviesList.innerHTML = movies.map(movie => `
                    <div class="bg-white rounded-xl shadow-md overflow-hidden card-hover">
                        <img src="${movie.posterUrl ? `${API_URL}/${movie.posterUrl.replace(/^\//, '')}` : `${API_URL}/images/movie-posters/default.jpg`}"
                             alt="${movie.title}"
                             class="w-full h-56 object-cover"
                             onerror="this.src='${API_URL}/images/movie-posters/default.jpg'">
                        <div class="p-6">
                            <h3 class="text-xl font-semibold mb-3 text-gray-900">${movie.title}</h3>
                            <p class="text-gray-600 mb-2">Thể loại: ${movie.genre || 'Chưa cập nhật'}</p>
                            <p class="text-gray-600 mb-4">Thời lượng: ${movie.duration || 0} phút</p>
                            <div class="flex justify-end space-x-3">
                                <button onclick="editMovie(${movie.id})"
                                        class="btn bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
                                    Sửa
                                </button>
                                <button onclick="deleteMovie(${movie.id})"
                                        class="btn bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-600">
                                    Xóa
                                </button>
                            </div>
                        </div>
                    </div>
                `).join('');
            } else {
                moviesList.innerHTML = `
                    <div class="col-span-full text-center text-gray-500">
                        <p>Chưa có phim nào.</p>
                    </div>
                `;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            moviesList.innerHTML = `
                <div class="col-span-full text-center text-red-500">
                    <p>Có lỗi xảy ra khi tải danh sách phim: ${error.message}</p>
                    <button onclick="loadMovies()" class="mt-3 btn bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700">
                        Thử lại
                    </button>
                </div>
            `;
        });
}

function showAddMovieForm() {
    document.getElementById('form-title').textContent = 'Thêm Phim Mới';
    document.getElementById('movieId').value = '';
    document.getElementById('movieForm').reset();
    document.getElementById('movie-form').classList.remove('hidden');
    loadMovieStatuses();
}

function hideMovieForm() {
    document.getElementById('movie-form').classList.add('hidden');
    document.getElementById('movieForm').reset();
}

function loadMovieStatuses() {
    fetch(`${API_URL}/api/movies/statuses`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Không thể tải danh sách trạng thái');
        }
        return response.json();
    })
    .then(statuses => {
        const statusSelect = document.getElementById('status');
        statusSelect.innerHTML = statuses.map(status =>
            `<option value="${status.value}">${status.label}</option>`
        ).join('');
    })
    .catch(error => {
        console.error('Error loading statuses:', error);
        document.getElementById('status').innerHTML = '<option value="">Lỗi tải trạng thái</option>';
    });
}

function editMovie(movieId) {
    fetch(`${API_URL}/api/movies/public/${movieId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải thông tin phim');
            }
            return response.json();
        })
        .then(movie => {
            console.log('Movie data:', movie);
            window.currentMovie = movie;
            document.getElementById('form-title').textContent = 'Sửa Phim';
            document.getElementById('movieId').value = movie.id;
            document.getElementById('title').value = movie.title || '';
            document.getElementById('genre').value = movie.genre || '';
            document.getElementById('director').value = movie.director || '';
            document.getElementById('movieCast').value = movie.cast || '';
            document.getElementById('duration').value = movie.duration || '';
            document.getElementById('releaseDate').value = movie.releaseDate ? movie.releaseDate.split('T')[0] : '';
            document.getElementById('description').value = movie.description || '';
            loadMovieStatuses();
            setTimeout(() => {
                document.getElementById('status').value = movie.status;
            }, 500);
            document.getElementById('movie-form').classList.remove('hidden');
        })
        .catch(error => {
            console.error('Error fetching movie:', error);
            alert('Có lỗi xảy ra khi tải thông tin phim: ' + error.message);
        });
}

function deleteMovie(movieId) {
    if (confirm('Bạn có chắc chắn muốn xóa phim này?')) {
        const movieElement = document.querySelector(`[onclick="deleteMovie(${movieId})"]`).closest('.bg-white');
        movieElement.innerHTML = `
            <div class="p-6 text-center">
                <div class="spinner"></div>
                <p class="mt-3 text-gray-600">Đang xóa phim...</p>
            </div>
        `;

        fetch(`${API_URL}/api/movies/${movieId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(async response => {
            if (!response.ok) {
                throw new Error(await response.text() || 'Không thể xóa phim');
            }
            await new Promise(resolve => setTimeout(resolve, 1000));
            alert('Xóa phim thành công');
            await loadMovies();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi xảy ra khi xóa phim: ' + error.message);
            loadMovies();
        });
    }
}

// Xử lý form thêm/sửa phim
document.getElementById('movieForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const submitButton = this.querySelector('button[type="submit"]');
    submitButton.disabled = true;
    submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';

    try {
        if (!window.currentUser || !window.currentUser.roles || !window.currentUser.roles.includes('ROLE_ADMIN')) {
            alert('Bạn không có quyền thực hiện thao tác này!');
            return;
        }

        const movieId = document.getElementById('movieId').value;
        const posterFile = document.getElementById('posterFile').files[0];

        if (movieId) {
            const movieData = {
                title: document.getElementById('title').value,
                genre: document.getElementById('genre').value,
                director: document.getElementById('director').value,
                movieCast: document.getElementById('movieCast').value,
                duration: parseInt(document.getElementById('duration').value),
                releaseDate: document.getElementById('releaseDate').value + 'T00:00:00',
                description: document.getElementById('description').value,
                status: document.getElementById('status').value
            };

            if (window.currentMovie && window.currentMovie.posterUrl) {
                movieData.posterUrl = window.currentMovie.posterUrl;
            }

            const updateResponse = await fetch(`${API_URL}/api/movies/${movieId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(movieData)
            });

            if (!updateResponse.ok) {
                throw new Error(await updateResponse.text() || 'Không thể cập nhật phim');
            }

            if (posterFile) {
                const formData = new FormData();
                formData.append('posterFile', posterFile);
                const posterResponse = await fetch(`${API_URL}/api/movies/${movieId}/poster`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    },
                    body: formData
                });
                if (!posterResponse.ok) {
                    console.warn('Poster upload failed but movie data was updated');
                }
            }

            alert('Cập nhật phim thành công!');
        } else {
            const formData = new FormData();
            formData.append('title', document.getElementById('title').value);
            formData.append('genre', document.getElementById('genre').value);
            formData.append('director', document.getElementById('director').value);
            formData.append('movieCast', document.getElementById('movieCast').value);
            formData.append('duration', document.getElementById('duration').value);
            formData.append('releaseDate', document.getElementById('releaseDate').value + 'T00:00:00');
            formData.append('description', document.getElementById('description').value);
            formData.append('status', document.getElementById('status').value);
            if (posterFile) {
                formData.append('posterFile', posterFile);
            }

            const response = await fetch(`${API_URL}/api/movies`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Server response:', errorText);
                if (response.status === 401) {
                    alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!');
                    window.location.href = '../login.html';
                    return;
                }
                if (response.status === 403) {
                    alert('Bạn không có quyền thực hiện thao tác này!');
                    return;
                }
                throw new Error(errorText || 'Không thể thêm phim mới. Vui lòng kiểm tra kết nối và thử lại!');
            }

            alert('Thêm phim mới thành công');
        }

        await new Promise(resolve => setTimeout(resolve, 1000));
        hideMovieForm();
        let retryCount = 0;
        const maxRetries = 3;
        while (retryCount < maxRetries) {
            try {
                await loadMovies();
                break;
            } catch (error) {
                console.warn(`Lần thử ${retryCount + 1}: Không thể tải lại danh sách phim`, error);
                retryCount++;
                if (retryCount === maxRetries) {
                    console.error('Không thể tải lại danh sách phim sau nhiều lần thử');
                }
                await new Promise(resolve => setTimeout(resolve, 1000));
            }
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Có lỗi xảy ra: ' + error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = 'Lưu';
    }
});