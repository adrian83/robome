

const Reducer = (state, action) => {
    console.log("action", action)
    console.log("state", state)
    
    switch (action.type) {
        case 'STORE_JWT_TOKEN':
            return { ...state, authToken: action.authToken };
        case 'REMOVE_JWT_TOKEN':
                return { ...state, authToken: null };
        default:
            return state;
        }
};

export default Reducer;