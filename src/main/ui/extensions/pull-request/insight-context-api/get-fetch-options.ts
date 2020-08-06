const getFetchOptions = (methodType: string) => ({
    method: methodType,
    headers: {
        'Content-Type': 'application/json',
    },
});

export default getFetchOptions;
