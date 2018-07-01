export const noParent = {
  configStack: [
    {
      elements: {
        add: [
          {
            id: 'combinedParser',
            type: 'CombinedParser',
          },
          {
            id: 'xsltFilter',
            type: 'XSLTFilter',
          },
          {
            id: 'Source',
            type: 'Source',
          },
        ],
        remove: [],
      },
      properties: {
        add: [
          {
            element: 'xsltFilter',
            name: 'xsltNamePattern',
            value: {
              string: 'DSD',
              integer: null,
              entity: null,
              boolean: null,
              long: null,
            },
          },
        ],
        remove: [],
      },
      pipelineReferences: {
        add: [],
        remove: [],
      },
      links: {
        add: [
          {
            from: 'Source',
            to: 'combinedParser',
          },
          {
            from: 'combinedParser',
            to: 'xsltFilter',
          },
        ],
        remove: [],
      },
    },
  ],
  merged: {
    elements: {
      add: [
        {
          id: 'xsltFilter',
          type: 'XSLTFilter',
        },
        {
          id: 'combinedParser',
          type: 'CombinedParser',
        },
        {
          id: 'Source',
          type: 'Source',
        },
      ],
      remove: [],
    },
    properties: {
      add: [
        {
          element: 'xsltFilter',
          name: 'xsltNamePattern',
          value: {
            string: 'DSD',
            integer: null,
            entity: null,
            boolean: null,
            long: null,
          },
        },
      ],
      remove: [],
    },
    pipelineReferences: {
      add: [],
      remove: [],
    },
    links: {
      add: [
        {
          from: 'combinedParser',
          to: 'xsltFilter',
        },
        {
          from: 'Source',
          to: 'combinedParser',
        },
      ],
      remove: [],
    },
  },
};

export const parentNoProperty = {
  configStack: [
    {
      elements: {
        add: [
          { id: 'combinedParser', type: 'CombinedParser' },
          { id: 'xsltFilter', type: 'XSLTFilter' },
          { id: 'Source', type: 'Source' },
        ],
        remove: [],
      },
      properties: {
        add: [
          {
            element: 'xsltFilter',
            name: 'xsltNamePattern',
            value: {
              string: 'DSD',
              integer: null,
              entity: null,
              boolean: null,
              long: null,
            },
          },
        ],
        remove: [],
      },
      pipelineReferences: { add: [], remove: [] },
      links: {
        add: [
          { from: 'Source', to: 'combinedParser' },
          { from: 'combinedParser', to: 'xsltFilter' },
        ],
        remove: [],
      },
    },
    {
      elements: { add: [], remove: [] },
      properties: {
        add: [
          {
            element: 'combinedParser',
            name: 'type',
            value: {
              string: 'JSON',
              integer: null,
              entity: null,
              boolean: null,
              long: null,
            },
          },
          {
            element: 'xsltFilter',
            name: 'xsltNamePattern',
            value: {
              string: 'D',
              integer: null,
              entity: null,
              boolean: null,
              long: null,
            },
          },
        ],
        remove: [],
      },
      pipelineReferences: { add: [], remove: [] },
      links: { add: [], remove: [] },
    },
  ],
  merged: {
    elements: {
      add: [
        { id: 'xsltFilter', type: 'XSLTFilter' },
        { id: 'combinedParser', type: 'CombinedParser' },
        { id: 'Source', type: 'Source' },
      ],
      remove: [],
    },
    properties: {
      add: [
        {
          element: 'xsltFilter',
          name: 'xsltNamePattern',
          value: {
            string: 'D',
            integer: null,
            entity: null,
            boolean: null,
            long: null,
          },
        },
        {
          element: 'combinedParser',
          name: 'type',
          value: {
            string: 'JSON',
            integer: null,
            entity: null,
            boolean: null,
            long: null,
          },
        },
      ],
      remove: [],
    },
    pipelineReferences: { add: [], remove: [] },
    links: {
      add: [{ from: 'combinedParser', to: 'xsltFilter' }, { from: 'Source', to: 'combinedParser' }],
      remove: [],
    },
  },
};

export const parentWithProperty = {
  configStack: [
    {
      elements: {
        add: [
          { id: 'combinedParser', type: 'CombinedParser' },
          { id: 'xsltFilter', type: 'XSLTFilter' },
          { id: 'Source', type: 'Source' },
        ],
        remove: [],
      },
      properties: {
        add: [
          {
            element: 'combinedParser',
            name: 'type',
            value: {
              string: 'JS', integer: null, entity: null, boolean: null, long: null,
            },
          },
          {
            element: 'xsltFilter',
            name: 'xsltNamePattern',
            value: {
              string: 'DSD', integer: null, entity: null, boolean: null, long: null,
            },
          },
        ],
        remove: [],
      },
      pipelineReferences: { add: [], remove: [] },
      links: {
        add: [
          { from: 'Source', to: 'combinedParser' },
          { from: 'combinedParser', to: 'xsltFilter' },
        ],
        remove: [],
      },
    },
    {
      elements: { add: [], remove: [] },
      properties: {
        add: [
          {
            element: 'combinedParser',
            name: 'type',
            value: {
              string: 'JSON', integer: null, entity: null, boolean: null, long: null,
            },
          },
          {
            element: 'xsltFilter',
            name: 'xsltNamePattern',
            value: {
              string: 'D', integer: null, entity: null, boolean: null, long: null,
            },
          },
        ],
        remove: [],
      },
      pipelineReferences: { add: [], remove: [] },
      links: { add: [], remove: [] },
    },
  ],
  merged: {
    elements: {
      add: [
        { id: 'xsltFilter', type: 'XSLTFilter' },
        { id: 'combinedParser', type: 'CombinedParser' },
        { id: 'Source', type: 'Source' },
      ],
      remove: [],
    },
    properties: {
      add: [
        {
          element: 'xsltFilter',
          name: 'xsltNamePattern',
          value: {
            string: 'D', integer: null, entity: null, boolean: null, long: null,
          },
        },
        {
          element: 'combinedParser',
          name: 'type',
          value: {
            string: 'JSON', integer: null, entity: null, boolean: null, long: null,
          },
        },
      ],
      remove: [],
    },
    pipelineReferences: { add: [], remove: [] },
    links: {
      add: [{ from: 'combinedParser', to: 'xsltFilter' }, { from: 'Source', to: 'combinedParser' }],
      remove: [],
    },
  },
};
