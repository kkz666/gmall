var comment = {

    api_name: '/api/comment/commentInfo',

    save(commentInfo) {
        return request({
            url: this.api_name + `/auth/save`,
            method: 'post',
            data: commentInfo
        })
    },

    getPageList(spuId, page, limit) {
        return request({
            url: this.api_name + `/${spuId}/${page}/${limit}`,
            method: 'get'
        })
    },

    countBySpuId(spuId) {
        return request({
            url: this.api_name + `/countBySpuId/${spuId}`,
            method: 'get'
        })
    }
}
