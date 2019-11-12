

const Reducer = (state, action) => {
    console.log("state", state)
    console.log("action", action)
    switch (action.type) {
        case 'STORE_JWT_TOKEN':
            return { ...state, authenticated: action.authenticated };
        case 'REMOVE_JWT_TOKEN':
                return { ...state, authenticated: null };
        default:
            return state;
        }
};

export default Reducer;